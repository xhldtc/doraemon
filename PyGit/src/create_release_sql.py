from string import Template
import glob
import os
import pyodbc
import re
import shutil

config = dict(line.strip().split('=') for line in open('../config/db.properties'))

db = pyodbc.connect('DRIVER={SQL Server Native Client 10.0};SERVER=%s;DATABASE=%s;UID=%s;PWD=%s' % (config['server'], config['db'], config['user'], config['pwd']))
svn_bin = 'C:/Users/E531864/Desktop/Apache-Subversion-1.9.2/bin/svn'
dev_sql_path = 'https://hzhwd087.corp.statestr.com/svn/rat/trunks/RAT/sql_scripts/MYRTS_AGILE/MSSQL/UAT/CD'
release_sql_path = 'https://hzhwd087.corp.statestr.com/svn/rat/trunks/RAT/sql_scripts/MYRTS_AGILE_AUTO/UAT'
auto_dir = '../svnworkspace/UAT/'
base_dir = '../svnworkspace/CD'
version = '260'

def get_file_name(f):
	return os.path.splitext(os.path.basename(f))[0]
	
def add_go_to_end(s):
	s = s.strip()
	if s.endswith('GO') or s.endswith('go'):
		return s + '\n'
	else:
		return s + '\nGO\n'

def create_next_release_folder(version):
	next_version = int(version) + 1
	next_dir = base_dir + str(next_version)
	if os.system(svn_bin + ' ls ' + dev_sql_path + str(next_version)) == 1:
		print next_version, "not exist, create new"
		os.makedirs(next_dir)
		os.makedirs(next_dir + "/Data")
		os.makedirs(next_dir + "/Function")
		os.makedirs(next_dir + "/Procedure")
		os.makedirs(next_dir + "/Table")
		os.makedirs(next_dir + "/View")
		name = "CD%d_MYRTS_Agile_Release.sql" % next_version
		f = open(next_dir + "/" + name, 'w+')
		f.write(open('../template/release_template.sql').read())
		f.close()
		os.system(svn_bin + ' import -m newRelease ' + next_dir + ' ' + dev_sql_path + str(next_version))
		shutil.rmtree(next_dir)
		print 'create folder success!'
	else:
		print next_version, "already exist"

def is_table_function(name):
	cursor = db.cursor()
	cursor.execute(" select OBJECT_ID (N'%s', N'TF')" % name)
	if cursor.fetchone()[0] is not None:
		return True
	cursor = db.cursor()
	cursor.execute(" select OBJECT_ID (N'%s', N'IF')" % name)
	if cursor.fetchone()[0] is not None:
		return True
	return False

def check_out_from_svn(base_folder):
	base_folder = base_dir + version
	
	if os.path.exists(base_folder):
		os.system(svn_bin + ' update ' + base_folder)
	else:
		os.system(svn_bin + ' checkout ' + dev_sql_path + version + ' ' + base_folder)
		
	if os.path.exists(auto_dir):
		os.system(svn_bin + ' update ' + auto_dir)
	else:
		os.system(svn_bin + ' checkout ' + release_sql_path + ' ' + auto_dir)
		
def integrate_scripts(base_folder):
	mp = dict()
	all_table = []
	all_procedure = []
	all_view = []
	all_function = []
	
	data_files = glob.glob(base_folder + "/Data/*.*")
	data = ""
	for f in data_files:
		data += add_go_to_end(open(f).read()) + "\n"
	mp['data_scripts'] = data
	
	function_files = glob.glob(base_folder + "/Function/*.*")
	function = ""
	for f in function_files:
		all_function.append(get_file_name(f))
		function += add_go_to_end(open(f).read()) + "\n"
	mp['function_scripts'] = function
	
	procedure_files = glob.glob(base_folder + "/Procedure/*.*")
	procedure = ""
	for f in procedure_files:
		all_procedure.append(get_file_name(f))
		procedure += add_go_to_end(open(f).read()) + "\n"
	mp['procedure_scripts'] = procedure
	
	table_files = glob.glob(base_folder + "/Table/*.*")
	table = ""
	for f in table_files:
		all_table.append(get_file_name(f))
		table += add_go_to_end(open(f).read()) + "\n"
	mp['table_scripts'] = table
	
	view_files = glob.glob(base_folder + "/View/*.*")
	view = ""
	for f in view_files:
		all_view.append(get_file_name(f))
		view += add_go_to_end(open(f).read()) + "\n"
	mp['view_scripts'] = view
	
	roles = ["APPLICATION_UPDATE", "APPLICATION_READONLY", "DMM_UPDATE"]
	execute = "GRANT EXECUTE ON %s TO %s\n"
	other = "GRANT SELECT, UPDATE, DELETE, INSERT ON %s TO %s\n"
	select = "GRANT SELECT ON %s TO %s\n"
	
	grant = ""
	for func in all_function:
		if is_table_function(func):
			grant += select % (func, roles[0])
			grant += select % (func, roles[1])
			grant += select % (func, roles[2])
		else:
			grant += execute % (func, roles[0])
			grant += execute % (func, roles[2])
		grant += "GO\n\n"
	for proc in all_procedure:
		grant += execute % (proc, roles[0])
		grant += execute % (proc, roles[2])
		grant += "GO\n\n" 
	
	for tab in all_table:
		grant += other % (tab, roles[0])
		grant += select % (tab, roles[1])
		grant += other % (tab, roles[2])
		grant += "GO\n\n"
		
	for vie in all_view:
		grant += other % (vie, roles[0])
		grant += select % (vie, roles[1])
		grant += other % (vie, roles[2])
		grant += "GO\n\n"
	
	mp['grant_scripts'] = grant
	return mp

def main():
	base_folder = base_dir + version
	check_out_from_svn(base_folder)
	
	mp = integrate_scripts(base_folder)
	
	sql_template = open('../template/sql_template.sql')
	result = Template(sql_template.read()).safe_substitute(mp)
	
	release_file = open(base_folder + '/CD' + version + '_MYRTS_Agile_Release.sql', 'w+')
	release_file.write(result)
	
	for f in os.listdir(auto_dir):
		if re.search('^CD\d+_MYRTS_Agile_Release.sql$', f):
			os.system(svn_bin + ' delete ' + os.path.join(auto_dir, f))
	
	filename = 'CD%s_MYRTS_Agile_Release.sql' % (version)
	open(auto_dir + '/' + filename, 'w+').write('USE &&APP_SCHEMA\n' + result)
	open(auto_dir + '/install.sql', 'w+').write('@./' + filename + '\n')
	
	sql_template.close()
	release_file.close()
	os.system(svn_bin + ' add ' + auto_dir + '/' + filename)
	os.system(svn_bin + ' commit -m scripts ' + base_folder)
	os.system(svn_bin + ' commit -m delpoyScripts ' + auto_dir)
	print "release success!!!"
	create_next_release_folder(version)

if __name__ == "__main__":
	main()
