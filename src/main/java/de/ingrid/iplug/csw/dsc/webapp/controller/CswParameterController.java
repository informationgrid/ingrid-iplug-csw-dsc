/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.csw.dsc.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.controller.AbstractController;
import de.ingrid.iplug.csw.dsc.webapp.object.CswConfiguration;
import de.ingrid.iplug.csw.dsc.webapp.validation.CswParameterValidator;
import de.ingrid.utils.PlugDescription;

/**
 * Control the csw parameter page.
 * 
 * @author joachim@wemove.com
 * 
 */
@Controller
@SessionAttributes("plugDescription")
public class CswParameterController extends AbstractController {
    private final CswParameterValidator _validator;

    @Autowired
    public CswParameterController(CswParameterValidator validator) {
        _validator = validator;
    }

    @RequestMapping(value = { "/iplug-pages/welcome.html",
            "/iplug-pages/cswParams.html" }, method = RequestMethod.GET)
    public String getParameters(
            final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject) {

        CswConfiguration cswConfig = new CswConfiguration();
        
        mapConfigFromPD(cswConfig, commandObject);
        
        // write object into session
        modelMap.addAttribute("cswConfig", cswConfig);
        
        return AdminViews.CSW_PARAMS;
    }

    @RequestMapping(value = "/iplug-pages/cswParams.html", method = RequestMethod.POST)
    public String post(
            @ModelAttribute("cswConfig") final CswConfiguration commandObject,
            final BindingResult errors,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject pdCommandObject) {

        // check if page contains any errors
        if (_validator.validateCswParams(errors).hasErrors()) {
            return AdminViews.CSW_PARAMS;
        }

        // put values into plugdescription
        mapParamsToPD(commandObject, pdCommandObject);

        return AdminViews.SAVE;
    }

    private void mapParamsToPD(CswConfiguration commandObject,
            PlugdescriptionCommandObject pdCommandObject) {

        pdCommandObject.put("serviceUrl", commandObject.getServiceUrl());

        pdCommandObject.setRankinTypes(true, false, false);

        // add necessary fields so iBus actually will query us
        // remove field first to prevent multiple equal entries
        pdCommandObject.removeFromList(PlugDescription.FIELDS, "incl_meta");
        pdCommandObject.addField("incl_meta");
        pdCommandObject.removeFromList(PlugDescription.FIELDS, "t01_object.obj_class");
        pdCommandObject.addField("t01_object.obj_class");
        pdCommandObject.removeFromList(PlugDescription.FIELDS, "metaclass");
        pdCommandObject.addField("metaclass");

        // add required datatypes to PD
        // -> is added in GeneralController with forced added datatype!
        //pdCommandObject.addDataType("dsc_csw");
        //pdCommandObject.addDataType("csw");
        //pdCommandObject.addDataType("IDF_1.0");
    }

    private void mapConfigFromPD(CswConfiguration mapConfig,
            PlugdescriptionCommandObject commandObject) {
        
        if (commandObject.containsKey("serviceUrl")) {
            mapConfig.setServiceUrl(commandObject.getString("serviceUrl"));
        }
    }
    

}
