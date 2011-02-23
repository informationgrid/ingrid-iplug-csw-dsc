package de.ingrid.iplug.csw.dsc.webapp.validation;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;

import de.ingrid.admin.validation.AbstractValidator;
import de.ingrid.iplug.csw.dsc.webapp.object.CswConfiguration;

/**
 * Validator for csw parameter dialog.
 * 
 * 
 * @author joachim@wemove.com
 *
 */
@Service
public class CswParameterValidator extends
        AbstractValidator<CswConfiguration> {

    public final Errors validateCswParams(final BindingResult errors) {
        rejectIfEmptyOrWhitespace(errors, "serviceUrl");
        return errors;
    }
}
