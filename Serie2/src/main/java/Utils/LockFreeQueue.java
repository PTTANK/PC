package Utils;

import java.util.concurrent.atomic.AtomicReference;

public class LockFreeQueue<T> {

    static class Node<T> {
        volatile T value;
        final AtomicReference<Node<T>> next = new AtomicReference<>();
        public Node(T value) {
            this.value = value;
        }
    }

    private final AtomicReference<Node<T>> head;
    private final AtomicReference<Node<T>> tail;

    public LockFreeQueue() {
        Node<T> dummy = new Node<T>(null);
        head = new AtomicReference<>(dummy);
        tail = new AtomicReference<>(dummy);
    }

    public void enqueue(T value) {
        Node<T> node = new Node<>(value);

        while(true) {
            Node<T> observedTail = tail.get();
            Node<T> observedTailNext = observedTail.next.get();
            if (observedTailNext != null) {
                tail.compareAndSet(observedTail, observedTailNext);
                continue;
            }
            if(observedTail.next.compareAndSet(null, node)) {
                tail.compareAndSet(observedTail, node);
                return;
            }
        }
    }

    public T dequeue(){
        while(true){
            if(isEmpty()) return null;
            Node<T> head = this.head.get();
            Node<T> firstElem = head.next.get();
            if(this.head.compareAndSet(head, firstElem)){
                T value = firstElem.value;
                head.value = null;
                return value;
            }
        }
    }

    public boolean isEmpty() {
        return head.get() == tail.get();
    }
}
