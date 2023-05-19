package com.car.sales.company.services;

import com.car.sales.company.exceptions.DatoInvalidoException;
import com.car.sales.company.models.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.car.sales.company.helper.ValidacionHelper.validarPositivoDecimal;
import static com.car.sales.company.models.NombreNotificacion.*;
import static com.car.sales.company.models.TipoUsuario.COMPRADOR;
import static com.car.sales.company.models.TipoUsuario.VENDEDOR;

public class VentaService {
    NotificacionService notificacionService;

    public VentaService(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }


    public Publicacion interactuar(Publicacion publicacion, Usuario usuario, Accion accion, double nuevoMonto) {
        switch (accion) {
            case CONTRA_OFERTAR:
                return interactuarConContraOferta(publicacion, usuario, nuevoMonto);
            case ACEPTAR_OFERTA:
                return interactuarConAceptar(publicacion, usuario);
            case RETIRAR_OFERTA:
                return interactuarConRetirar(publicacion, usuario);
            case OFERTAR:
                return interactuarConOfertar(publicacion, usuario, nuevoMonto);
        }
        return publicacion;
    }

    private Publicacion interactuarConOfertar(Publicacion publicacion, Usuario usuario, double monto) {
        if (usuario.getTipoUsuario().equals(COMPRADOR)) {
            Oferta oferta = new Oferta(validarPositivoDecimal(monto), 0, usuario, LocalDateTime.now());
            NombreNotificacion nombreNotificacion;
            if (publicacion.getOfertasCompradores().isEmpty()) {
                nombreNotificacion = COMPRADOR_PRIMERA_OFERTA;
                publicacion.setOfertasCompradores(Collections.singletonList(oferta));
            } else {
                nombreNotificacion = COMPRADOR_NUEVA_OFERTA;
                publicacion.getOfertasCompradores().add(oferta);
            }
            notificacionService.enviarNotificacion(publicacion.getVendedor(), publicacion.getProducto(), monto,
                    0, nombreNotificacion);
        }
        return publicacion;
    }

    private Publicacion interactuarConContraOferta(Publicacion publicacion, Usuario comprador, double nuevoMonto) {
        if (comprador.getTipoUsuario().equals(COMPRADOR)) {
            Oferta ofertaActual = encontrarOfertaUsuario(publicacion, comprador);
            ofertaActual.setMontoContraOferta(nuevoMonto);
            notificacionService.enviarNotificacion(comprador, publicacion.getProducto(), ofertaActual.getMontoOferta(),
                    ofertaActual.getMontoContraOferta(), VENDEDOR_CONTRAOFERTA);
        } else {
            throw new DatoInvalidoException("El usuario debe ser comprador");
        }
        return publicacion;
    }

    private Publicacion interactuarConAceptar(Publicacion publicacion, Usuario usuario) {
        Oferta mejorOferta;
        NombreNotificacion nombreNotificacion = null;
        if (usuario.getTipoUsuario().equals(VENDEDOR)) {
            mejorOferta = obtenerMayorOferta(publicacion.getOfertasCompradores());
            nombreNotificacion = VENDEDOR_ACEPTA_OFERTA;
            usuario = mejorOferta.getComprador();
        } else {
            mejorOferta = encontrarOfertaUsuario(publicacion, usuario);
            if (mejorOferta.getMontoContraOferta() > 0) {
                nombreNotificacion = COMPRADOR_ACEPTA_OFERTA;
                usuario = publicacion.getVendedor();
            }
        }
        notificacionService.enviarNotificacion(usuario, publicacion.getProducto(),
                mejorOferta.getMontoOferta(), mejorOferta.getMontoContraOferta(), nombreNotificacion);
        notificarCompradoresVehiculoVendido(publicacion, mejorOferta);
        publicacion.setEstaDisponibleEnLaWeb(false);

        return publicacion;
    }

    private Oferta encontrarOfertaUsuario(Publicacion publicacion, Usuario usuario) {
        for (Oferta ofertaActual : publicacion.getOfertasCompradores()) {
            if (usuarioTieneOferta(usuario, ofertaActual)) {
                return ofertaActual;
            }
        }
        throw new DatoInvalidoException("Usuario no tiene oferta en esta Publicacion");
    }

    private Publicacion interactuarConRetirar(Publicacion publicacion, Usuario usuario) {
        if (usuario.getTipoUsuario().equals(COMPRADOR)) {
            Oferta ofertaActual = encontrarOfertaUsuario(publicacion, usuario);
            ofertaActual.setInactivo(true);
            notificacionService.enviarNotificacion(publicacion.getVendedor(), publicacion.getProducto(),
                    ofertaActual.getMontoOferta(), ofertaActual.getMontoContraOferta(), COMPRADOR_RETIRA_OFERTA);


        } else {
            throw new DatoInvalidoException("El usuario debe ser de tipo comprador");
        }
        return publicacion;
    }

    private boolean usuarioTieneOferta(Usuario usuario, Oferta oferta) {
        return usuario.getIdentificacion().equals(oferta.getComprador().getIdentificacion());
    }

    private void notificarCompradoresVehiculoVendido(Publicacion publicacion, Oferta mejorOferta) {
        for (Oferta ofertaActual : publicacion.getOfertasCompradores()) {
            if (!ofertaActual.getComprador().getIdentificacion().equals(mejorOferta.getComprador().getIdentificacion())) {
                ofertaActual.setInactivo(true);
                notificacionService.enviarNotificacion(ofertaActual.getComprador(), publicacion.getProducto(),
                        0, 0, VEHICULO_NO_DISPONIBLE);
            }
        }
    }

    private Oferta obtenerMayorOferta(List<Oferta> ofertasCompradores) {
        double montoMayor = 0;
        Oferta ofertaMontoMayor = null;
        for (Oferta ofertaActual : ofertasCompradores) {
            if (ofertaActual.getMontoOferta() > montoMayor) {
                montoMayor = ofertaActual.getMontoOferta();
                ofertaMontoMayor = ofertaActual;
            }
            if (ofertaActual.getMontoOferta() == montoMayor && ofertaMontoMayor != null &&
                    ofertaActual.getFechaOferta().isBefore(ofertaMontoMayor.getFechaOferta())) {
                ofertaMontoMayor = ofertaActual;
            }
        }
        return ofertaMontoMayor;
    }
}

