package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {

	
    private List<Tuple> tuplespace;

    
    /** Constructor. */
    public CentralizedLinda() {
        this.tuplespace = new ArrayList<Tuple>();
    }
    

    /** Adds a tuple t to the tuplespace. */
    public void write(Tuple t) {
        this.tuplespace.add(t);
    }

    
    /** Returns a tuple matching the template and removes it from the tuplespace.
     * Blocks if no corresponding tuple is found. */
    public  synchronized Tuple take(Tuple template) {
        if (this.tuplespace.contains(template)) {
            this.tuplespace.remove(template);
            return tuplespace.get(tuplespace.indexOf(template));
        }
        else {
        	return null;
            // BLOQUER LA METHODE AVEC UN MONITEUR
        }
    }

    
    /** Returns a tuple matching the template and leaves it in the tuplespace.
     * Blocks if no corresponding tuple is found. */
    public Tuple read(Tuple template) {
        if (this.tuplespace.contains(template)) {
            return tuplespace.get(tuplespace.indexOf(template));
        }
        else {
        	return null;
            // BLOQUER LA METHODE AVEC UN MONITEUR
        }
    }

    
    /** Returns a tuple matching the template and removes it from the tuplespace.
     * Returns null if none found. */
    public Tuple tryTake(Tuple template) {
        if (this.tuplespace.contains(template)) {
            this.tuplespace.remove(template);
            return tuplespace.get(tuplespace.indexOf(template));
        }
        else {
            return null;
        }
    }
    
    
    /** Returns a tuple matching the template and leaves it in the tuplespace.
     * Returns null if none found. */
    public  Tuple tryRead(Tuple template) {
        if (this.tuplespace.contains(template)) {
            return tuplespace.get(tuplespace.indexOf(template));
        }
        else {
            return null;
        }
    }

    
    /** Returns all the tuples matching the template and removes them from the tuplespace.
     * Returns an empty collection if none found (never blocks).
     * Note: there is no atomicity or consistency constraints between takeAll and other methods;
     * for instance two concurrent takeAll with similar templates may split the tuples between the two results.
     */
    public Collection<Tuple> takeAll(Tuple template) {
        ArrayList<Tuple> listeTemplate = new ArrayList<Tuple>();
        for (Tuple element : tuplespace) {
            if (element==template) {
                listeTemplate.add(element);
                this.tuplespace.remove(element);
            }
        }
        return listeTemplate;
    }

    
    /** Returns all the tuples matching the template and leaves them in the tuplespace.
     * Returns an empty collection if none found (never blocks).
     * Note: there is no atomicity or consistency constraints between readAll and other methods;
     * for instance (write([1]);write([2])) || readAll([?Integer]) may return only [2].
     */
	public Collection<Tuple> readAll(Tuple template) {
		return null;
	}

	
	/** Registers a callback which will be called when a tuple matching the template appears.
     * If the mode is Take, the found tuple is removed from the tuplespace.
     * The callback is fired once. It may re-register itself if necessary.
     * If timing is immediate, the callback may immediately fire if a matching tuple is already present; if timing is future, current tuples are ignored.
     * Beware: a callback should never block as the calling context may be the one of the writer (see also {@link AsynchronousCallback} class).
     * Callbacks are not ordered: if more than one may be fired, the chosen one is arbitrary.
     * Beware of loop with a READ/IMMEDIATE re-registering callback !
     *
     * @param mode read or take mode.
     * @param timing (potentially) immediate or only future firing.
     * @param template the filtering template.
     * @param callback the callback to call if a matching tuple appears.
     */
	public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
		
	}

	
    /** To debug, prints any information it wants (e.g. the tuples in tuplespace or the registered callbacks), prefixed by <code>prefix</code. */
	public void debug(String prefix) {
		
	}

	
}
