package com.car.sales.company.exceptions;

public class UsuarioNoEncontradoException extends RuntimeException{

    public UsuarioNoEncontradoException(String mensaje){
        super(mensaje);
    }
}
