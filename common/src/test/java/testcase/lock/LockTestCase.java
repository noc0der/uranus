package testcase.lock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.redisson.Redisson;
import org.redisson.core.RLock;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LockTestCase {
	//
	Redisson redisson = null;
	//
	public void after() {
        try {
            redisson.flushdb();
        } finally {
            redisson.shutdown();
        }
    }
	@Test
	public void test() {
		redisson = null;
		try {
			redisson = Redisson.create();
			RLock lock = redisson.getLock("key");
			lock.lock(3, TimeUnit.SECONDS);
			final long startTime = System.currentTimeMillis();
			final CountDownLatch latch = new CountDownLatch(1);
			//
			new Thread() {
				public void run() {
					RLock lock1 = redisson.getLock("key");
					lock1.lock();
					long spendTime = System.currentTimeMillis() - startTime;
					Assert.assertTrue(spendTime < 3020);
					lock1.unlock();
					latch.countDown();
				};
			}.start();

			latch.await();

			lock.unlock();
		} catch (Exception e) {
			Assert.fail("soso", e);
		} finally {
			after();
		}
	}
	@Test
	public void test1(){//测试一下并行
		try {
			redisson = Redisson.create();
			long start = System.currentTimeMillis();
			RLock lock = redisson.getLock("key1");
			lock.lock(300, TimeUnit.SECONDS);
			long end = System.currentTimeMillis();
			System.out.println("lock...1:"+(end-start));
			System.out.println("locked:"+lock.isLocked());
			System.out.println("held:"+lock.isHeldByCurrentThread());
			//
			start = System.currentTimeMillis();
			RLock lock1 = redisson.getLock("key1");
			
//			lock1.lock(3, TimeUnit.SECONDS);
			lock1.lock(3, TimeUnit.SECONDS);
			System.out.println("locked(2nd):"+lock.isLocked());
			System.out.println("held(2nd):"+lock.isHeldByCurrentThread());
			end = System.currentTimeMillis();
			System.out.println("lock...2:"+(end-start));
		} catch (Exception e) {
			Assert.fail("soso1", e);
		}finally{
			after();
		}
		
	}
}
