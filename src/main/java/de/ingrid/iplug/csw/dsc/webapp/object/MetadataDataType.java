package de.ingrid.iplug.csw.dsc.webapp.object;

import org.springframework.stereotype.Service;

import de.ingrid.admin.object.AbstractDataType;

@Service
public class MetadataDataType extends AbstractDataType {

    public MetadataDataType() {
        super("metadata");
        setForceActive(true);
    }

}