package de.ingrid.iplug.csw.dsc.cswclient;

public interface CSWRequestPreprocessor<T extends Object> {
    
    public T process(T param);

}
