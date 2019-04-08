package com.company.arminro.viewmodel;

import android.net.Uri;

import javax.xml.validation.Validator;

public class UrlValidator {


    // blatantly simple logic to validate if a string is an url
    static boolean Validate (String raw){

        return !raw.contains("mailto") && (raw.contains("http") || raw  .contains("www"));
    }
}
