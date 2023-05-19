package com.car.sales.company.models;

import java.time.LocalDate;
import java.util.List;

public class Publicacion {
    private Usuario vendedor;
    private Producto producto;
    private LocalDate fecha;
    private double precio;
    private List<Oferta> ofertasCompradores;
    private boolean estaDisponibleEnLaWeb;



    public Usuario getVendedor() {
        return vendedor;
    }

    public void setVendedor(Usuario vendedor) {
        this.vendedor = vendedor;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public List<Oferta> getOfertasCompradores() {
        return ofertasCompradores;
    }

    public void setOfertasCompradores(List<Oferta> ofertasCompradores) {
        this.ofertasCompradores = ofertasCompradores;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public boolean isEstaDisponibleEnLaWeb() {
        return estaDisponibleEnLaWeb;
    }

    public void setEstaDisponibleEnLaWeb(boolean estaDisponibleEnLaWeb) {
        this.estaDisponibleEnLaWeb = estaDisponibleEnLaWeb;
    }
}
