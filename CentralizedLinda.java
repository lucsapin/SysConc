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
    private Condition condRead;				// conditions pour mettre en attente et reveiller les threads
    private Condition condTake;
    private Map<Thread, Tuple> demandeurs;	// map des threads avec le tuple demandé
    private Map<Tuple, List<Abonnement>> listeAttente;

    
    /** Constructor. */
    public CentralizedLinda() {
        this.tuplespace = new ArrayList<Tuple>();
        this.lock = new ReentrantLock();
        this.condRead = lock.newCondition();
        this.condTake = lock.newCondition();
        this.demandeurs = new HashMap<Thread, Tuple>();
        this.listeAttente = new HashMap<Tuple, List<Abonnement>>();
    }
    

    /** Adds a tuple t to the tuplespace.
     */
    public synchronized void write(Tuple t) {
    	this.lock.lock();
    	// Ajouter le tuple au tuplespace
        this.tuplespace.add(t);
        
        // Itérateur pour parcourir la map
        Iterator<Entry<Thread, Tuple>> it = demandeurs.entrySet().iterator();
        while (it.hasNext()) {
        	Map.Entry<Thread, Tuple> pair = (Entry<Thread, Tuple>) it.next();
        	// Si un thread écrit un tuple qui matche le tuple demandé par un autre thread en attente
        	if (pair.getValue().matches(t)) {
        		// Notifier le thread en attente de lecture
        		this.condRead.notify();
        		// Notifier le thread en attente de take
        		this.condTake.notify();
        		// Enlever le thread de la map des demandeurs
        		this.demandeurs.remove(pair.getKey());
        		this.lock.unlock();
        		break;
        	}
        }
    }

    
    /** Returns a tuple matching the template and removes it from the tuplespace.
     * Blocks if no corresponding tuple is found.
     */
    public synchronized Tuple take(Tuple template) {	
    	this.lock.lock();
    	Tuple resultat = null;
    	boolean trouve = false;
    	
    	// Parcourir la liste des tuples
        for (Tuple tuple : this.tuplespace) {
        	// Si un tuple matche le template demandé
        	if (tuple.matches(template)) {
        		// Retourner le tuple et l'enlever du tuplespace
        		resultat = tuple;
                this.tuplespace.remove(tuple);
                trouve = true;
                // Déverouiller le lock et sortir de la boucle for
                lock.unlock();
                break;
	        }
        }
        // Si on ne trouve pas de tuple correspondant
        if (!trouve) {
        	try {
        		// Ajouter le thread et le tuple qu'il demande aux demandeurs
        		this.demandeurs.put(Thread.currentThread(), template);
        		// Bloquer le thread
	        	this.condTake.await();
	        	// Une fois le thread réveillé, appel récursif sur take
	        	resultat = take(template);
        	} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				// Dans tous les cas, déverouiller le lock
				this.lock.unlock();
	        }
        }
        return resultat;        
    }

    
    /** Returns a tuple matching the template and leaves it in the tuplespace.
     * Blocks if no corresponding tuple is found.
     */
    public synchronized Tuple read(Tuple template) {
    	this.lock.lock();
    	Tuple resultat = null;
    	boolean trouve = false;
    	
    	// Parcourir la liste des tuples
    	for (Tuple tuple : this.tuplespace) {
    		// Si un tuple matche le template demandé
    		if (tuple.matches(template)) {
    			// Retourner le tuple
    			resultat = tuple;
                trouve = true;
                // Déverouiller le lock et sortir de la boucle for
                lock.unlock();
                break;
	        }
        }
    	// Si on ne trouve pas de tuple correspondant
        if (!trouve) {
        	try {
        		// Ajouter le thread et le tuple qu'il demande aux demandeurs
        		this.demandeurs.put(Thread.currentThread(), template);
        		// Bloquer le thread
	        	this.condRead.await();
	        	// Une fois le thread réveillé, appel récursif sur read
	        	resultat = read(template);
        	} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				// Dans tous les cas, déverouiller le lock
				this.lock.unlock();
	        }
        }        
        return resultat;
    }

    
    /** Returns a tuple matching the template and removes it from the tuplespace.
     * Returns null if none found.
     */
    public Tuple tryTake(Tuple template) {
    	Tuple temp = null;
    	
    	for (Tuple tuple : this.tuplespace) {
    		if (tuple.matches(template)) {
    			temp = tuple;
    			this.tuplespace.remove(tuple);
    			break;
    		}
    	}
    	
    	return temp;
    }
    
    
    /** Returns a tuple matching the template and leaves it in the tuplespace.
     * Returns null if none found.
     */
    public  Tuple tryRead(Tuple template) {
    	Tuple temp = null;
    	
    	for (Tuple tuple : this.tuplespace) {
    		if (tuple.matches(template)) {
    			temp = tuple;
    			break;
    		}
    	}
    	
    	return temp;
    }

    
    /** Returns all the tuples matching the template and removes them from the tuplespace.
     * Returns an empty collection if none found (never blocks).
     * Note: there is no atomicity or consistency constraints between takeAll and other methods;
     * for instance two concurrent takeAll with similar templates may split the tuples between the two results.
     */
    public Collection<Tuple> takeAll(Tuple template) {
        List<Tuple> tupleList = new ArrayList<Tuple>();
        
        for (Tuple element : tuplespace) {
            if (element.matches(template)) {
                tupleList.add(element);
                this.tuplespace.remove(element);
            }
        }
        
        return tupleList;
    }

    
    /** Returns all the tuples matching the template and leaves them in the tuplespace.
     * Returns an empty collection if none found (never blocks).
     * Note: there is no atomicity or consistency constraints between readAll and other methods;
     * for instance (write([1]);write([2])) || readAll([?Integer]) may return only [2].
     */
	public Collection<Tuple> readAll(Tuple template) {
		List<Tuple> tupleList = new ArrayList<Tuple>();
        for (Tuple element : tuplespace) {
            if (element.matches(template)) {
            	tupleList.add(element);
            }
        }
        return tupleList;
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
		Tuple temp = null;
		
		// Timing immédiat
		if (timing == eventTiming.IMMEDIATE) {
			// Essayer read ou take en fonction du mode
			if (mode == eventMode.READ) temp = tryRead(template);
			if (mode == eventMode.TAKE) temp = tryTake(template);
			// Si un matching tuple existe dans le tuplespace
			if (temp != null) {
				// Callback immédiat
				callback.call(temp);
			} else {
				// Sinon mettre en attente le callback
				enregistrer(template, callback, mode);
			}
		}
		
		// Timing futur
		if (timing == eventTiming.FUTURE) {
			// Ignorer les tuples déjà présents et mettre en attente le callback
			enregistrer(template, callback, mode);
		}
		
	}

	
    /** To debug, prints any information it wants (e.g. the tuples in tuplespace or the registered callbacks), prefixed by <code>prefix</code. */
	public void debug(String prefix) {
		System.out.println("Debug " + prefix + " :");
		int i = 1;
        for (Tuple tuple : tuplespace) {
        	System.out.println("Tuple n°" + i + " : " + tuple.toString());
        	i++;
        }
	}
	
	/** Enregistrer un callback 
	 * 
	 * @param template
	 * @param callback
	 * @param mode
	 */
	public void enregistrer(Tuple template, Callback callback, eventMode mode) {
		Abonnement abo = new Abonnement(mode, callback);
		
		// Vérifier si la liste des abonnements au template est vide
		if (listeAttente.get(template) == null) {
			// Si elle est vide, la créer et...
			List<Abonnement> liste = new ArrayList<Abonnement>();
			// ... y ajouter l'abonnement
			liste.add(abo);
			listeAttente.put(template, liste);
		} else {
			listeAttente.get(template).add(abo);
		}
	}

	
}
