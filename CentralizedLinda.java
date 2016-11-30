package linda.shm;

import linda.Linda;
import linda.Tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {

    private List<Tuple> tuplespace;

    public CentralizedLinda() {
        this.tuplespace = new ArrayList<Tuple>();
    }

    public void write(Tuple t) {
        this.tuplespace.add(t);
    }

    public  synchronized Tuple take(Tuple template) {

        if (this.tuplespace.contains(template)) {
            this.tuplespace.remove(template);
            return tuplespace.get(tuplespace.indexOf(template));
        }
        else {
            // BLOQUER LA METHODE AVEC UN MONITEUR
        }
    }

    public Tuple read(Tuple template) {
        if (this.tuplespace.contains(template)) {
            return tuplespace.get(tuplespace.indexOf(template));
        }
        else {
            // BLOQUER LA METHODE AVEC UN MONITEUR
        }
    }

    public Tuple tryTake(Tuple template) {
        if (this.tuplespace.contains(template)) {
            this.tuplespace.remove(template);
            return tuplespace.get(tuplespace.indexOf(template));
        }
        else {
            return null;
        }
    }

    public  Tuple tryRead(Tuple template) {
        if (this.tuplespace.contains(template)) {
            return tuplespace.get(tuplespace.indexOf(template));
        }
        else {
            return null;
        }
    }

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

}
