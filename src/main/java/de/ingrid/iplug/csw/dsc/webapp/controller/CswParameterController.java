/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.csw.dsc.webapp.controller;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.controller.AbstractController;
import de.ingrid.iplug.csw.dsc.Configuration;
import de.ingrid.iplug.csw.dsc.webapp.object.CswConfiguration;
import de.ingrid.iplug.csw.dsc.webapp.validation.CswParameterValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

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

    private final Configuration cswConfig;

    @Autowired
    public CswParameterController(CswParameterValidator validator, Configuration cswConfig) {
        _validator = validator;
        this.cswConfig = cswConfig;
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

        cswConfig.serviceUrl= commandObject.getServiceUrl();

        // add required datatypes to PD
        // -> is added in GeneralController with forced added datatype!
        //pdCommandObject.addDataType("dsc_csw");
        //pdCommandObject.addDataType("csw");
        //pdCommandObject.addDataType("IDF_1.0");
    }

    private void mapConfigFromPD(CswConfiguration mapConfig,
            PlugdescriptionCommandObject commandObject) {
        mapConfig.setServiceUrl(cswConfig.serviceUrl);
    }
    

}
