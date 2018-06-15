package xdire.con;

public class ConcurrentLinkedSet<T> {

    private volatile Node head = null;
    private volatile Node tail = null;
    private volatile int length = 0;

    /**
     *  Construct Linked Set
     *  -----------------------------------------
     *  Apply head node as a starting ref
     *  Define tail
     */
    public ConcurrentLinkedSet() {
        head = new Node();
        tail = head;
    }

    /**
     *  Add to set
     *  -----------------------------------------
     *  @param value    : Generic value
     *  @return         : return is bool operation
     */
    public boolean add(T value) {

        // Define pointers
        Node pred = null;
        Node curr = null;

        // Assign pointers
        pred = head;
        curr = pred.next;

        // Pre-count element key
        int key = value.hashCode();

        // Do Search
        while (curr != null && curr.key <= key) {

            pred = curr;
            curr = curr.next;

        }

        /*
            After search we will be having 2 of possible
            situations:
            ---------------------------------------------
            1 - prev defined && curr is NULL, which mean
                we are or at very beginning with length 0
                or at the end adding to tail

            2 - prev defined && curr defined, which mean
                we in the middle of set
         */

        try {

            /*
                For provide guarantees on correct operation
                order we should always lock first element
                which can be affected by deletion first
                prior to element which happens to be a
                referral
             */

            // Lock previous Node (always presented)
            pred.lock();

            // Lock current Node if it's presented
            if(curr != null)
                curr.lock();

            // Pass the validation to determine if
            // both locked nodes are currently valid
            // to proceed with operation (check validation)
            if(validate(pred, curr)) {

                // Check that we not having this value
                // already defined in set
                if(pred.key == key)

                    return true;

                // Create new Node
                Node node = new Node(value);

                // Situation 2 - paste in the middle
                if(curr != null) {

                    node.next = curr;
                    pred.next = node;

                }
                // Situation 1 - paste beginning or end
                else {

                    pred.next = node;
                    tail = node;

                }

                // Update length
                length++;
                return true;

            }

        } finally {

            // Release locks after return
            if(curr != null)

                curr.unlock();

            pred.unlock();

        }

        return false;

    }

    /**
     *  Remove from Set
     *  -----------------------------------------
     *  @param value    : Generic value
     *  @return         : bool return
     */
    public boolean remove(T value) {

        //Return TRUE on empty set removal
        if(length == 0)

            return  true;

        // Pre-count element key
        int key = value.hashCode();

        //Return TRUE on values that doesn't present in Set
        if(tail.key < key)

            return  true;

        // Define pointers
        Node pred = null;
        Node curr = null;

        // Point pointers
        pred = head;
        curr = pred.next;

        // Do search
        while (curr.key <= key) {

            // Eliminate hash collisions
            if(value.equals(curr.value)) {
                break;
            }

            pred = curr;
            curr = curr.next;

        }

        /*
            Only one situation possible after search
            is that pred and curr defined because we
            will work with filled set and if value
            presented in set it will come to be in
            current
         */
        try {

            /*
                For provide guarantees on correct operation
                order we should always lock first element
                which can be affected by deletion first
                prior to element which happens to be a
                referral
             */
            pred.lock();
            curr.lock();

            // Validate that after lock we still have
            // situation we can delete elements
            if(validate(pred, curr)) {

                // Eliminate Hash collisions
                if(curr.value.equals(value)) {

                    // Remap references
                    pred.next = curr.next;
                    curr.marked = true;

                    // Remap tail
                    if (curr == tail) {
                        tail = pred;
                    }

                    // Decrement length
                    length--;

                }

                return  true;

            }

        } catch (Exception e) {

            System.out.println("Error happened during ConcurrentLinkedSet.remove() operation");

        } finally {

            // Unlock both after return happen
            pred.unlock();
            curr.unlock();
        }

        return false;

    }

    /**
     *  Process validation
     *  -----------------------------------------
     *  Nodes will be compared on 2 of following
     *  - Non marked for sweep
     *  - Reference linkage is in tact
     *  @param pred : Validate node happens before
     *                  current node
     *  @param curr : Validate node happens to be
     *                  current node
     *  @return     : boolean result
     */
    private boolean validate(Node pred, Node curr) {

        if(curr != null) {
            return !pred.marked && !curr.marked && pred.next == curr;
        } else {
            return !pred.marked;
        }


    }

    /**
     *  Set serialization
     *  -----------------------------------------
     *  @return : Comma-delimited string
     */
    @Override
    public String toString() {

        Node pred = null;
        Node curr = null;

        pred = head;
        curr = pred.next;

        StringBuffer sb = new StringBuffer();

        while(curr != null) {

            if(!curr.marked) {

                // Append key information
                sb.append(curr.key).append(':').append(curr.value);

                // Unless ref is Tail append comma-delimiter
                if (curr != tail) {
                    sb.append(',');
                }

            }

            curr = curr.next;

        }

        return sb.toString();

    }

    /**
     *  Private Node class
     *  -----------------------------------------
     *  Simple structure to carry value ref, key
     *  next ref and bits for mark-to-sweep and
     *  element level locking
     */
    private class Node {

        // Prevent ref caching
        volatile Node next = null;
        // As value doesn't change for Set operations
        // leave if Thread-cacheable
        T value;
        // keys doesn't change leave as cacheable
        int key;
        // Prevent mark-sweep caching
        volatile boolean marked = false;
        // Prevent lock to cache
        volatile boolean locked = false;

        /**
         *  Construct Node with no value as Minimal Node
         *  -----------------------------------------
         */
        Node() {
            this.key = Integer.MIN_VALUE;
        }

        /**
         *  Construct Node with value as normal Node
         *  -----------------------------------------
         *  @param value
         */
        Node(T value) {
            this.value = value;
            this.key = value.hashCode();
        }

        /**
         *  Lock Node
         *  -----------------------------------------
         *  Lock node to prevent any possible update
         *  operation performed on top of it
         */
        void lock() {

            while (locked) {
                // wait for be unlocked
            }

            this.locked = true;

        }

        /**
         *  Lock release
         *  -----------------------------------------
         *  For operations can be performed with
         *  this Node
         */
        void unlock() {

            this.locked = false;

        }

    }

}
