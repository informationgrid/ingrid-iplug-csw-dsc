package de.ingrid.iplug.csw.dsc.webapp.object;

import org.springframework.stereotype.Service;

import de.ingrid.admin.object.AbstractDataType;

@Service
public class CswDataType extends AbstractDataType {

    public CswDataType() {
        super("csw");
        setForceActive(true);
    }

}