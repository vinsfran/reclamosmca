package py.gov.mca.reclamosmca.sessionbeans;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import py.gov.mca.reclamosmca.entitys.PermisosElementosWeb;

/**
 *
 * @author vinsfran
 */
@Stateless
public class PermisosElementosWebSB {

    @PersistenceContext(unitName = "reclamosmcaPU")
    private EntityManager em;
    private String mensajes = "";

    public String crearPermisosElementosWeb(PermisosElementosWeb objeto) {
        mensajes = "";
        try {
            em.persist(objeto);
            em.flush();
            mensajes = "OK";
        } catch (Exception ex) {
            mensajes = ex.getMessage();
        }
        return mensajes;
    }

    public String actualizarPermisosElementosWeb(PermisosElementosWeb objeto) {
        mensajes = "";
        try {
            em.merge(objeto);
            em.flush();
            mensajes = "OK";
        } catch (Exception ex) {
            mensajes = ex.getMessage();
        }
        return mensajes;
    }

    public String eliminarPermisosElementosWeb(PermisosElementosWeb objeto) {
        mensajes = "";
        try {
            em.remove(objeto);
            mensajes = "OK";
        } catch (Exception ex) {
            mensajes = ex.getMessage();
        }
        return mensajes;
    }

    @SuppressWarnings("unchecked")
    public PermisosElementosWeb consultarPermisosElementosWeb(Integer codPermisoElementoWeb) {
        Query q = em.createNamedQuery("PermisosElementosWeb.findByCodPermisoElementoWeb");
        q.setParameter("codPermisoElementoWeb", codPermisoElementoWeb);
        return (PermisosElementosWeb) q.getResultList().get(0);
    }

    @SuppressWarnings("unchecked")
    public PermisosElementosWeb consultarCodRolCodElementoWeb(Integer codRol, Integer codElementoWeb) {
        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT e ");
        jpql.append("FROM PermisosElementosWeb e ");
        jpql.append("WHERE e.fkCodRol.codRol = :paramCodRol ");
        jpql.append("AND e.fkCodElementoWeb.codElementoWeb = :paramCodElementoWeb ");
        //jpql.append("WHERE e.persona.nombre LIKE '%:paramNombre%'");
        Query q = em.createQuery(jpql.toString());
        q.setParameter("paramCodRol", codRol);
        q.setParameter("paramCodElementoWeb", codElementoWeb);
        if (q.getResultList().isEmpty()) {
            return null;
        } else {
            return (PermisosElementosWeb) q.getResultList().get(0);
        }

    }

    @SuppressWarnings("unchecked")
    public List<PermisosElementosWeb> listarPermisosElementosWeb() {
        Query q = em.createNamedQuery("PermisosElementosWeb.findAll");
        return q.getResultList();
    }
}
