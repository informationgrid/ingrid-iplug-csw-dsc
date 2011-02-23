package de.ingrid.iplug.csw.dsc.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.controller.ExtrasController;

/**
 * A controller to activate the ExtrasController with more settings.
 * 
 * @author joachim@wemove.com
 * 
 */
@Controller
@SessionAttributes("plugDescription")
public class CswDscExtrasController {

    /**
     * The main purpose is to show the option, that the result shall be also
     * shown on the right side in the portal.
     * 
     * @param controller
     */
    @Autowired
    public CswDscExtrasController(ExtrasController controller) {
        controller.show_ShowInUnranked();
    }

}
