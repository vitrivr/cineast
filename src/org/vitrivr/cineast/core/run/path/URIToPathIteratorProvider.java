package org.vitrivr.cineast.core.run.path;

import org.vitrivr.cineast.core.run.ExtractionCompleteListener;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class URIToPathIteratorProvider implements PathIteratorProvider, ExtractionCompleteListener{

    private final Iterator<URI> uris;

    public URIToPathIteratorProvider(Iterator<URI> uris){
        this.uris = uris;
    }

    @Override
    public Iterator<Path> getPaths() {
        return new URIToPathIterator();
    }

    @Override
    public void onCompleted(Path path) {

    }

    private class URIToPathIterator implements Iterator<Path>{

        private Path next = null;

        URIToPathIterator(){
            tryGetNext();
        }


        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Path next() {
            try{
                return next;
            }finally {
                tryGetNext();
            }
        }

        private void tryGetNext(){
            while(uris.hasNext()){

                URI uri = uris.next();

                if(uri == null){
                    continue;
                }

                try{
                    next = Paths.get(uri);
                    return;
                }catch (Exception e){
                    //ignore
                }

                //TODO handle different types of URIs which cannot directly be transformed to paths here...

            }
            next = null;
        }

    }

}
