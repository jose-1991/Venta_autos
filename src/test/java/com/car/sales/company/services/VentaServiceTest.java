package com.car.sales.company.services;

import com.car.sales.company.exceptions.DatoInvalidoException;
import com.car.sales.company.models.Oferta;
import com.car.sales.company.models.Publicacion;
import com.car.sales.company.models.Usuario;
import com.car.sales.company.models.Vehiculo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

import static com.car.sales.company.models.Accion.*;
import static com.car.sales.company.models.TipoUsuario.COMPRADOR;
import static com.car.sales.company.models.TipoUsuario.VENDEDOR;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class VentaServiceTest {

    private double montoOferta;
    private double montoContraOferta;
    private Oferta oferta;
    private Oferta oferta2;
    private Oferta oferta3;
    private Usuario comprador;
    private Usuario comprador2;
    private Usuario comprador3;
    private Usuario vendedor;
    private Vehiculo vehiculo;
    private Publicacion publicacion;

    @Mock
    private NotificacionService notificacionService;
    @InjectMocks
    private VentaService ventaService;

    @Before
    public void setUp() {

        montoOferta = 1730;
        montoContraOferta = 1203;
        comprador = new Usuario("Ruben", "Sanchez", "ci", "5203746",
                "rube.123-122@gmail.com", COMPRADOR, null);
        comprador2 = new Usuario("Joel", "Sanchez", "ci", "1245362",
                "rube.123-122@gmail.com", COMPRADOR, null);
        comprador3 = new Usuario("Jenny", "Soria", "ci", "9763642",
                "rube.123-122@gmail.com", COMPRADOR, null);

        vendedor = new Usuario("Jorge", "Lopez", "ci", "5203717",
                "jorgito-122@gmail.com", VENDEDOR, null);

        vehiculo = new Vehiculo("1HGBH41JXMN109716", "Toyota", "Scion", 2020);

        oferta = new Oferta(17000, 0, comprador, LocalDateTime.now());
        oferta2 = new Oferta(14000, 0, comprador2, LocalDateTime.now());
        oferta3 = new Oferta(18600, 0, comprador3, LocalDateTime.now());

        publicacion = new Publicacion();
        publicacion.setVendedor(vendedor);
        publicacion.setProducto(vehiculo);
        publicacion.setEstaDisponibleEnLaWeb(true);
        publicacion.setFecha(LocalDate.now());
        publicacion.setPrecio(18000);

        publicacion.setOfertasCompradores(new ArrayList<>());
        publicacion.getOfertasCompradores().add(oferta);
        publicacion.getOfertasCompradores().add(oferta2);
        publicacion.getOfertasCompradores().add(oferta3);
    }

    @Test
    public void testInteractuarCaseContraOfertarCaseVendedor() {
        montoContraOferta = 19000;

        Publicacion publicacionActual = ventaService.interactuar(publicacion, comprador, CONTRA_OFERTAR, montoContraOferta);

        assertEquals(montoContraOferta, publicacionActual.getOfertasCompradores().get(0).getMontoContraOferta(), 0.0);
        verify(notificacionService).enviarNotificacion(any(), any(), anyDouble(), anyDouble(), any());
    }

    @Test(expected = DatoInvalidoException.class)
    public void testInteractuarCaseContraOfertarBotaExceptionCuandoElUsuarioEsVendedor() {
        montoContraOferta = 19000;

        ventaService.interactuar(publicacion, vendedor, CONTRA_OFERTAR, montoContraOferta);
    }

    @Test
    public void testInteractuarCaseAceptarOfertaCaseVendedorCuandoHayUnaMejorOferta() {
        oferta3.setMontoOferta(19000);
        Publicacion publicacionActual = ventaService.interactuar(publicacion, vendedor, ACEPTAR_OFERTA, 0);

        assertFalse(publicacionActual.isEstaDisponibleEnLaWeb());
        assertEquals(19000, publicacionActual.getOfertasCompradores().get(2).getMontoOferta(), 0.0);
        assertTrue(publicacionActual.getOfertasCompradores().get(0).isInactivo());
        assertTrue(publicacionActual.getOfertasCompradores().get(1).isInactivo());
        verify(notificacionService, times(3)).enviarNotificacion(any(), any(), anyDouble(), anyDouble(), any());
    }

    @Test
    public void testInteractuarCaseAceptarOfertaCaseVendedorCuandoHayDosMejoresOfertas() {
        oferta3.setMontoOferta(19000);
        oferta3.setFechaOferta(LocalDateTime.now());
        oferta2.setMontoOferta(19000);
        oferta2.setFechaOferta(LocalDateTime.now().minusHours(3));

        Publicacion publicacionActual = ventaService.interactuar(publicacion, vendedor, ACEPTAR_OFERTA, 0);

        assertFalse(publicacionActual.isEstaDisponibleEnLaWeb());
        assertEquals(19000, publicacionActual.getOfertasCompradores().get(1).getMontoOferta(), 0.0);
        assertTrue(publicacionActual.getOfertasCompradores().get(0).isInactivo());
        assertTrue(publicacionActual.getOfertasCompradores().get(2).isInactivo());
        verify(notificacionService, times(3)).enviarNotificacion(any(), any(), anyDouble(), anyDouble(), any());
    }

    @Test
    public void testInteractuarCaseAceptarOfertaCaseComprador() {
        oferta3.setMontoOferta(19000);
        oferta3.setMontoContraOferta(20000);
        Publicacion publicacionActual = ventaService.interactuar(publicacion, comprador3, ACEPTAR_OFERTA, 0);

        assertFalse(publicacionActual.isEstaDisponibleEnLaWeb());
        assertEquals(20000, publicacionActual.getOfertasCompradores().get(2).getMontoContraOferta(), 0.0);
        assertTrue(publicacionActual.getOfertasCompradores().get(0).isInactivo());
        assertTrue(publicacionActual.getOfertasCompradores().get(1).isInactivo());
        verify(notificacionService, times(3)).enviarNotificacion(any(), any(), anyDouble(), anyDouble(), any());

    }

    @Test(expected = DatoInvalidoException.class)
    public void testInteractuarCaseRetirarOfertaBotaExceptionCuandoElUsuarioEsVendedor() {

        ventaService.interactuar(publicacion, vendedor, RETIRAR_OFERTA, 0);
    }

    @Test
    public void testInteractuarCaseRetirarOfertaCaseComprador() {

        Publicacion publicacionActual = ventaService.interactuar(publicacion, comprador, RETIRAR_OFERTA, 0);

        assertTrue(publicacionActual.getOfertasCompradores().get(0).isInactivo());
        verify(notificacionService).enviarNotificacion(any(), any(), anyDouble(), anyDouble(), any());
    }

    @Test
    public void testInteractuarCaseOfertarCaseCompradorPrimeraOferta() {
        publicacion.setOfertasCompradores(Collections.EMPTY_LIST);
        montoOferta = 18000;

        Publicacion publicacionActual = ventaService.interactuar(publicacion, comprador, OFERTAR, montoOferta);

        assertEquals(1, publicacionActual.getOfertasCompradores().size());
        verify(notificacionService).enviarNotificacion(any(), any(), anyDouble(), anyDouble(), any());
    }

    @Test
    public void testInteractuarCaseOfertarCaseComprador() {
        montoOferta = 18000;

        Publicacion publicacionActual = ventaService.interactuar(publicacion, comprador, OFERTAR, montoOferta);

        assertEquals(4, publicacionActual.getOfertasCompradores().size());
        verify(notificacionService).enviarNotificacion(any(), any(), anyDouble(), anyDouble(), any());
    }

    @Test(expected = DatoInvalidoException.class)
    public void testRealizarPrimeraOfertaBotaExceptionCuandoNoSeIngresaUnMontoValido() {
        montoOferta = -4650;

        ventaService.interactuar(publicacion, comprador, OFERTAR, montoOferta);
    }

}