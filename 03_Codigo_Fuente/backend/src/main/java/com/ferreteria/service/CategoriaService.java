package com.ferreteria.service;

import com.ferreteria.dto.CategoriaRequest;
import com.ferreteria.dto.CategoriaResponse;
import com.ferreteria.entity.Categoria;
import com.ferreteria.exception.ApiException;
import com.ferreteria.repository.CategoriaRepository;
import com.ferreteria.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {
    private final CategoriaRepository categorias;
    private final ProductoRepository productos;

    public CategoriaService(CategoriaRepository categorias, ProductoRepository productos) {
        this.categorias = categorias; this.productos = productos;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categorias.findAll().stream().map(CategoriaResponse::de).toList();
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest r) {
        if (categorias.existsByNombreIgnoreCase(r.nombre().trim())) throw ApiException.conflict("La categoría ya existe");
        Categoria c = new Categoria();
        c.setNombre(r.nombre().trim()); c.setDescripcion(r.descripcion());
        return CategoriaResponse.de(categorias.save(c));
    }

    @Transactional
    public CategoriaResponse actualizar(Integer id, CategoriaRequest r) {
        Categoria c = categorias.findById(id).orElseThrow(() -> ApiException.notFound("Categoría no encontrada"));
        if (!c.getNombre().equalsIgnoreCase(r.nombre().trim()) && categorias.existsByNombreIgnoreCase(r.nombre().trim()))
            throw ApiException.conflict("La categoría ya existe");
        c.setNombre(r.nombre().trim()); c.setDescripcion(r.descripcion());
        return CategoriaResponse.de(categorias.save(c));
    }

    @Transactional
    public void eliminar(Integer id) {
        if (!categorias.existsById(id)) throw ApiException.notFound("Categoría no encontrada");
        if (productos.existsByCategoriaId(id)) throw ApiException.conflict("No se puede eliminar: la categoría tiene productos");
        categorias.deleteById(id);
    }
}
