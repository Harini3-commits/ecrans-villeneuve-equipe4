package com.ecrans.vente.adapter.in.web;

import com.ecrans.vente.application.exception.CommandeInconnue;
import com.ecrans.vente.application.exception.SeanceInconnue;
import com.ecrans.vente.application.port.out.ConflitDeVersion;
import com.ecrans.vente.domaine.commun.ErreurMetier;
import com.ecrans.vente.domaine.seance.PlacesIndisponibles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GestionErreurs {

    @ExceptionHandler({SeanceInconnue.class, CommandeInconnue.class})
    ProblemDetail ressourceInconnue(RuntimeException exception) {
        return probleme(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler({PlacesIndisponibles.class, ConflitDeVersion.class})
    ProblemDetail conflit(RuntimeException exception) {
        return probleme(HttpStatus.CONFLICT, exception);
    }

    @ExceptionHandler(ErreurMetier.class)
    ProblemDetail regleMetier(ErreurMetier exception) {
        return probleme(HttpStatus.UNPROCESSABLE_ENTITY, exception);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail requeteInvalide(IllegalArgumentException exception) {
        return probleme(HttpStatus.BAD_REQUEST, exception);
    }

    private static ProblemDetail probleme(HttpStatus status, RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(status, exception.getMessage());
    }
}
