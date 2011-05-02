package de.ingrid.iplug.csw.dsc.webapp.object;

import org.springframework.stereotype.Service;

import de.ingrid.admin.object.AbstractDataType;

@Service
public class DscCswDataType extends AbstractDataType {

    public DscCswDataType() {
        super("dsc_csw");
        setForceActive(true);
    }

}