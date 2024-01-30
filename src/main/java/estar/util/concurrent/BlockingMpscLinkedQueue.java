package estar.util.concurrent;

import org.jctools.queues.MpscLinkedQueue;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * This class is a BlockingQueue wrapper for JCTools' <a href="https://github.com/JCTools/JCTools/blob/master/jctools-core/src/main/java/org/jctools/queues/MpscLinkedQueue.java">MpscLinkedQueue</a>.
 * It is based on JCTools' <a href="https://github.com/JCTools/JCTools/blob/master/jctools-experimental/src/main/resources/org/jctools/queues/blocking/TemplateBlocking.java">TemplateBlocking.java</a>.
 * JCTools is available at <a href="https://github.com/JCTools/JCTools">JCTools GitHub Page</a> under the following license:
 * <a href="https://github.com/JCTools/JCTools/blob/master/LICENSE">JCTools License</a>
 */
public class BlockingMpscLinkedQueue<E> extends MpscLinkedQueue<E>  implements BlockingQueue<E> {

    @Override
    public void put(E e) {
        // Offering is always successful in MpscLinkedQueue, therefore nothing special to do here
        offer(e);
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) {
        return offer(e);
    }

    @Override
    public E take() throws InterruptedException {
        E e;
        while ((e = poll()) == null && !Thread.currentThread().isInterrupted())
            LockSupport.parkNanos(100_000);
        if (Thread.currentThread().isInterrupted())
            throw new InterruptedException("Thread was interrupted while waiting for element");
        return e;
    }


    @Override
    public E poll(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int remainingCapacity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int drainTo(Collection<? super E> collection)
    {
        int count = 0;
        E e;
        while ((e = poll()) != null) {
            collection.add(e);
            count++;
        }
        return count;
    }

    @Override
    public int drainTo(Collection<? super E> collection, int maxElements)
    {
        int count = 0;
        E e;
        while(((e = poll()) != null) && count < maxElements) {
            collection.add(e);
            count++;
        }
        return count;
    }
}
