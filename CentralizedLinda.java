package linda.shm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import linda.AsynchronousCallback;
import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {

	
	/** Attributes. */
    private List<Tuple> tuplespace;			// liste des tuples
    private ReentrantLock lock;				// lock pour controler l'acces aux ressources partagees par les threads
    private Condition attente;				// condition pour mettre en attente et reveiller les threads
    private Map<Thread, Tuple> demandeurs;	// map des threads avec le tuple demandé
    private Iterator it;					// pour iterer dans la map

    
    /** Constructor. */
    public CentralizedLinda() {
        this.tuplespace = new ArrayList<Tuple>();
        this.lock = new ReentrantLock();
        this.attente = lock.newCondition();
        this.demandeurs = new HashMap<Thread, Tuple>();
        this.it = demandeurs.entrySet().iterator();
    }
    

    /** Adds a tuple t to the tuplespace. */
    public synchronized void write(Tuple t) {
    	this.lock.lock();
        this.tuplespace.add(t);
        while (this.it.hasNext()) {
        	Map.Entry<Thread, Tuple> pair = (Entry<Thread, Tuple>) it.next();
        	if (pair.getValue().matches(t)) {
        		this.attente.notify();
        	}
        }
        this.lock.unlock();
    }

    
    /** Returns a tuple matching the template and removes it from the tuplespace.
     * Blocks if no corresponding tuple is found. */
    public synchronized Tuple take(Tuple template) {	
    	this.lock.lock();
    	Tuple resultat = null;
    	boolean trouve = false;
        for (Tuple tuple : this.tuplespace) {
        	if (tuple.matches(template)) {
        		resultat = this.tuplespace.get(tuplespace.indexOf(tuple));
                this.tuplespace.remove(tuple);
                trouve = true;
                lock.unlock();
                break;
	        }
        }
        if (!trouve) {
        	try {
        		this.demandeurs.put(Thread.currentThread(), template);
	        	this.attente.await();
	        	resultat = take(template);
        	} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				this.lock.unlock();
	        }
        }        
        return resultat;        
    }

    
    /** Returns a tuple matching the template and leaves it in the tuplespace.
     * Blocks if no corresponding tuple is found. */
    public synchronized Tuple read(Tuple template) {
    	this.lock.lock();
    	Tuple resultat = null;
    	boolean trouve = false;
    	for (Tuple tuple : this.tuplespace) {
    		if (tuple.matches(template)) {
    			resultat = this.tuplespace.get(tuplespace.indexOf(tuple));
                trouve = true;
                lock.unlock();
                break;
	        }
        }
        if (!trouve) {
        	try {
        		this.demandeurs.put(Thread.currentThread(), template);
	        	this.attente.await();
	        	resultat = read(template);
        	} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				this.lock.unlock();
	        }
        }        
        return resultat;
    }

    
    /** Returns a tuple matching the template and removes it from the tuplespace.
     * Returns null if none found. */
    public Tuple tryTake(Tuple template) {
        if (this.tuplespace.contains(template)) {
            this.tuplespace.remove(template);
            return tuplespace.get(tuplespace.indexOf(template));
        } else {
            return null;
        }
    }
    
    
    /** Returns a tuple matching the template and leaves it in the tuplespace.
     * Returns null if none found. */
    public  Tuple tryRead(Tuple template) {
        if (this.tuplespace.contains(template)) {
            return tuplespace.get(tuplespace.indexOf(template));
        } else {
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
            if (element == template) {
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
		ArrayList<Tuple> listeTemplate = new ArrayList<Tuple>();
        for (Tuple element : tuplespace) {
            if (element == template) {
                listeTemplate.add(element);
            }
        }
        return listeTemplate;
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
