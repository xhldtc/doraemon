package concurrent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

public class FIFOMutex {
	private final AtomicBoolean locked = new AtomicBoolean(false);
	private final Queue<Thread> waiters = new ConcurrentLinkedQueue<Thread>();

	public void lock() {
		boolean wasInterrupted = false;
		Thread current = Thread.currentThread();
		//waiters是一个queue,要获取锁的线程按顺序加入到队尾
		waiters.add(current);

		//要获取锁的条件：队首必须是当前线程，并且locked变量之前是false，原子地更新为true,
		//代表锁已经被获取，其他线程只能等待
		while (waiters.peek() != current || !locked.compareAndSet(false, true)) {
			//不满足获取锁条件的线程进入循环，调用park阻塞
			LockSupport.park(this);
			//park被唤醒和条件有三：一是unpark，二是中断，三是未知原因，如果这里是中断唤醒
			//先记录中断状态到临时变量，然后清空
			if (Thread.interrupted()) // ignore interrupts while waiting
				wasInterrupted = true;
		}
		//运行到这里，代表锁已经被当前线程获取，移除队首线程，即当前线程
		waiters.remove();
		//如果之前记录了中断状态，这里恢复，然后锁内的代码就可以响应中断操作
		if (wasInterrupted) // reassert interrupt status on exit
			current.interrupt();
	}

	public void unlock() {
		//释放锁的操作，先更新锁状态原子变量为false，然后unpark队首线程，让它可以从
		//park阻塞中恢复，获取锁
		locked.set(false);
		LockSupport.unpark(waiters.peek());
	}
}
