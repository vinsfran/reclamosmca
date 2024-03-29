package py.gov.mca.reclamosmca.beansession;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.validator.ValidatorException;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import maps.java.Geocoding;
//import maps.java.MapsJava;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.apache.commons.io.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.map.PointSelectEvent;
import org.primefaces.event.map.StateChangeEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import py.gov.mca.reclamosmca.entitys.Configuraciones;
import py.gov.mca.reclamosmca.entitys.EstadosReclamos;
import py.gov.mca.reclamosmca.entitys.EstadosUsuarios;
import py.gov.mca.reclamosmca.entitys.Imagenes;
import py.gov.mca.reclamosmca.entitys.Paises04Barrios;
import py.gov.mca.reclamosmca.entitys.Paises05Direcciones;
import py.gov.mca.reclamosmca.entitys.Personas;
import py.gov.mca.reclamosmca.entitys.Reclamos;
import py.gov.mca.reclamosmca.entitys.Roles;
import py.gov.mca.reclamosmca.entitys.TiposFinalizacionReclamos;
import py.gov.mca.reclamosmca.entitys.TiposReclamos;
import py.gov.mca.reclamosmca.entitys.Usuarios;
import py.gov.mca.reclamosmca.reportes.DependenciasReporte;
import py.gov.mca.reclamosmca.reportes.TiposReclamosCantidad;
import py.gov.mca.reclamosmca.reportes.TiposReclamosReporte;
import py.gov.mca.reclamosmca.sessionbeans.BarriosSB;
import py.gov.mca.reclamosmca.sessionbeans.ConfiguracionesSB;
import py.gov.mca.reclamosmca.sessionbeans.DireccionesSB;
import py.gov.mca.reclamosmca.sessionbeans.PersonasSB;
import py.gov.mca.reclamosmca.sessionbeans.ReclamosSB;
import py.gov.mca.reclamosmca.sessionbeans.TiposFinalizacionReclamosSB;
import py.gov.mca.reclamosmca.sessionbeans.TiposReclamosSB;
import py.gov.mca.reclamosmca.sessionbeans.UsuariosSB;
import py.gov.mca.reclamosmca.utiles.Converciones;

/**
 *
 * @author vinsfran kjsfkajshfjshka
 */
@ManagedBean(name = "mbSReclamos")
@SessionScoped
public class MbSReclamos implements Serializable {

    @EJB
    private ReclamosSB reclamosSB;

    @EJB
    private TiposReclamosSB tiposReclamosSB;
    @EJB
    private DireccionesSB direccionesSB;
    @EJB
    private BarriosSB barriosSB;
    @EJB
    private TiposFinalizacionReclamosSB tiposFinalizacionReclamosSB;
    @EJB
    private UsuariosSB usuariosSB;
    @EJB
    private PersonasSB personasSB;
    @EJB
    private ConfiguracionesSB configuracionesSB;

    private Configuraciones configuraciones;

    private List<Reclamos> misReclamos;
    private List<Reclamos> reclamosPendientes;
    private List<Reclamos> reclamosAtendidos;
    private List<Reclamos> reclamosFinalizados;
    private List<Reclamos> reclamosPorZona;
    private List<Reclamos> reclamos;
    private List<TiposReclamos> tiposDeReclamos;
    private List<Paises04Barrios> barrios;
    private List<TiposFinalizacionReclamos> listTiposFinalizacionReclamos;

    private TiposReclamos tipoDeReclamosSeleccionado;
    private TiposReclamos tipoDeReclamosAnterior;
    private Paises05Direcciones direccionSelecionada;
    private Paises04Barrios barrioSeleccionado;

    private Reclamos nuevoReclamo;
    private Reclamos reclamoSeleccionado;

    private Usuarios nuevoUsuario;

    private Imagenes imagenParaGuardar;

    private String dirReclamo;
    private String imagenSemaforo;
    private String mensajeCorreo;

    private Date fechaInicio;
    private Date fechaFin;

    private MapModel emptyModel;

    private int zoom;
    private int codigoEstadoReclamo;

    private LatLng latituteLongitude;

    private boolean mostrarGraphicImage;
    private boolean activarCamposNuevoUsuario;
    private boolean activarCampoCorreo;
    private boolean activarCampoDireccion;
    private boolean activarCamposCuenta;
    private boolean activarCamposTelefono;
    private boolean marcaParaNuevoUsuario;

    private DefaultStreamedContent imagenCargada;

    public MbSReclamos() {
        this.configuraciones = new Configuraciones();

    }

    @PostConstruct
    public void init() {
        emptyModel = new DefaultMapModel();
        configuraciones = configuracionesSB.consultarPorCodConfiguracion(2);
    }

    public String prepararNuevoReclamo() {
        this.emptyModel = null;
        this.emptyModel = new DefaultMapModel();
        this.nuevoReclamo = null;
        this.nuevoReclamo = new Reclamos();
        this.nuevoReclamo.setFkCodTipoReclamo(new TiposReclamos());
        this.nuevoReclamo.setLatitud(-25.3041049263554);
        this.nuevoReclamo.setLongitud(-57.5597266852856);
        this.nuevoReclamo.setFkCodDireccion(new Paises05Direcciones());
        this.nuevoReclamo.getFkCodDireccion().setFkCodBarrio(new Paises04Barrios());
        this.tipoDeReclamosSeleccionado = null;
        this.setMostrarGraphicImage(false);
        this.setZoom(15);
        this.imagenParaGuardar = null;
        this.imagenCargada = null;
        this.dirReclamo = "";
        this.configuraciones = configuracionesSB.consultarPorCodConfiguracion(2);
        return "admin_nuevo_reclamo";
    }

    public String prepararNuevoReclamoExterno() {
        this.activarCamposNuevoUsuario = false;
        this.activarCampoCorreo = false;
        this.activarCampoDireccion = false;
        this.activarCamposCuenta = false;
        this.activarCamposTelefono = false;
        this.marcaParaNuevoUsuario = false;
        this.mensajeCorreo = "";
        this.setNuevoUsuario(new Usuarios());
        this.getNuevoUsuario().setFkCodPersona(new Personas());
        this.emptyModel = null;
        this.emptyModel = new DefaultMapModel();
        this.nuevoReclamo = null;
        this.nuevoReclamo = new Reclamos();
        this.nuevoReclamo.setFkCodTipoReclamo(new TiposReclamos());
        this.nuevoReclamo.setLatitud(-25.3041049263554);
        this.nuevoReclamo.setLongitud(-57.5597266852856);

        this.nuevoReclamo.setFkCodDireccion(new Paises05Direcciones());
        this.nuevoReclamo.getFkCodDireccion().setFkCodBarrio(new Paises04Barrios());

        this.tipoDeReclamosSeleccionado = null;
        this.setMostrarGraphicImage(false);
        this.setZoom(15);
        this.imagenParaGuardar = null;
        this.imagenCargada = null;
        this.dirReclamo = "";
        return "admin_nuevo_reclamo_externo";
    }

    public String prepararReportePorEstadoRangoFecha() {
        this.codigoEstadoReclamo = 0;
        this.fechaInicio = new Date();
        this.fechaFin = new Date();

        return "admin_form_reporte_rango_fecha_estado_reclamo";
    }

    public String prepararReportePorEstadoRangoFechaTipo() {
        this.fechaInicio = new Date();
        this.fechaFin = new Date();

        return "admin_form_reporte_rango_fecha_dependencia_tipos_reclamos_estado";
    }

    public void seleccionarTipoDeReclamo(AjaxBehaviorEvent event) {
        this.tipoDeReclamosSeleccionado = tiposReclamosSB.consultarTipoReclamo(getNuevoReclamo().getFkCodTipoReclamo().getCodTipoReclamo());
        //tipoReclamo = tiposReclamosSB.consultarTipoReclamo(getReclamos().getFkCodTipoReclamo().getCodTipoReclamo());
        if (!emptyModel.getMarkers().isEmpty()) {
            emptyModel.getMarkers().get(0).setTitle(getTipoDeReclamosSeleccionado().getNombreTipoReclamo());
        }
    }

    public void seleccionarBarrio(AjaxBehaviorEvent event) {
        this.barrioSeleccionado = barriosSB.consultarBarrio(getNuevoReclamo().getFkCodDireccion().getFkCodBarrio().getCodBarrio());
        //this.dirReclamo = dirReclamo + " - Barrio: " + barrioSeleccionado.getBarrioNombre();
    }

    public void puntoSelecionado(PointSelectEvent event) throws UnsupportedEncodingException, MalformedURLException {
        if (this.tipoDeReclamosSeleccionado == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Por favor!", "Seleccione un tipo de reclamo."));
        } else {
            setLatituteLongitude(event.getLatLng());
            emptyModel = null;
            emptyModel = new DefaultMapModel();
            emptyModel.addOverlay(null);
            Marker marca = new Marker(getLatituteLongitude());
            marca.setTitle(getTipoDeReclamosSeleccionado().getNombreTipoReclamo());
            marca.setDraggable(true);
            emptyModel.addOverlay(marca);

            direccionSelecionada = direccionesSB.consultarDrireccionPorLatitudLongitud(getLatituteLongitude().getLat(), getLatituteLongitude().getLng());
            if (direccionSelecionada == null) {
                this.nuevoReclamo.setLatitud(getLatituteLongitude().getLat());
                this.nuevoReclamo.setLongitud(getLatituteLongitude().getLng());
                //Se inicializa con las configuraciones para el uso de mapas de Google
                setConfiguraciones(configuracionesSB.consultarPorCodConfiguracion(2));
//                String claveAPI = getConfiguraciones().getPar01();
//                Geocoding.setKey(claveAPI);
//                Geocoding.setSensor(false);
//                Geocoding.setLanguage("es");
//                Geocoding.setConnectTimeout(3600);
//
//                Geocoding objGeocod = new Geocoding();
//
//                int cantidadAddress = 0;
//                int conta = 0;
//                ArrayList<String> direcciones = new ArrayList<>();
//                boolean salir = true;
//                while (salir) {
//                    direcciones = objGeocod.getAddress(getLatituteLongitude().getLat(), getLatituteLongitude().getLng());
//                    cantidadAddress = direcciones.size();
//                    conta++;
//                    if (cantidadAddress > 0 || conta == 100) {
//                        salir = false;
//                    }
//                }

//                if (cantidadAddress > 0) {
//                    if (direcciones.get(0).toUpperCase().contains("ASUNCIÓN")) {
//                        setDirReclamo(objGeocod.getAddress(getLatituteLongitude().getLat(), getLatituteLongitude().getLng()).get(0));
//                        direccionSelecionada = new Paises05Direcciones();
//                        direccionSelecionada.setDireccionLatitud(getLatituteLongitude().getLat());
//                        direccionSelecionada.setDireccionLongitud(getLatituteLongitude().getLng());
//                        direccionSelecionada.setDireccionNombre(getDirReclamo());
//                        direccionSelecionada.setFkCodBarrio(barrioSeleccionado);
//                    } else {
//                        setDirReclamo("");
//                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "No se encuentra en Asunción", "Seleccione una ubicación valida."));
//                    }
//                } else {
//                    setDirReclamo("");
//                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "No se pudo encontrar la Dirección de su reclamo", "Vuelva a intentar."));
//                }
                direccionSelecionada = new Paises05Direcciones();
                direccionSelecionada.setDireccionLatitud(getLatituteLongitude().getLat());
                direccionSelecionada.setDireccionLongitud(getLatituteLongitude().getLng());
                direccionSelecionada.setDireccionNombre(getDirReclamo());
                direccionSelecionada.setFkCodBarrio(barrioSeleccionado);

            } else {
                setDirReclamo(direccionSelecionada.getDireccionNombre());
                nuevoReclamo.setFkCodDireccion(direccionSelecionada);
                this.barrioSeleccionado = direccionSelecionada.getFkCodBarrio();
            }

        }
    }

    public String enviarReclamo() throws Exception {
        Usuarios usu = recuperarUsuarioSession();
        if (this.imagenParaGuardar != null) {
            this.nuevoReclamo.setFkImagen(new Imagenes());
            this.nuevoReclamo.setFkImagen(this.imagenParaGuardar);
        }
        if (this.tipoDeReclamosSeleccionado == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Por favor!", "Seleccione un tipo de reclamo."));
            return "admin_nuevo_reclamo";
        } else if (emptyModel.getMarkers().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Por favor!", "Seleccione la ubicación de su reclamo."));
            return "admin_nuevo_reclamo";
        } else if (dirReclamo.equals("")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Por favor!", "Seleccione una ubicación valida."));
            return "admin_nuevo_reclamo";
        } else if (dirReclamo.equals("")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Por favor!", "Escriba la dirección de su reclamo."));
            return "admin_nuevo_reclamo";
        } else if (this.barrioSeleccionado == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Por favor!", "Seleccione un barrio."));
            return "admin_nuevo_reclamo";
        } else if (nuevoReclamo.getDescripcionReclamoContribuyente()
                .equals("")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Por favor!", "Detalle el reclamo."));
            return "admin_nuevo_reclamo";
        } else {
            nuevoReclamo.getFkCodDireccion().setDireccionNombre(getDirReclamo());
            nuevoReclamo.getFkCodDireccion().setDireccionLatitud(latituteLongitude.getLat());
            nuevoReclamo.getFkCodDireccion().setDireccionLongitud(latituteLongitude.getLng());

            if (direccionesSB.actualizarDireccion(nuevoReclamo.getFkCodDireccion()) != null) {
                nuevoReclamo.setFkCodUsuario(usu);
                nuevoReclamo.getFkCodUsuario().setFkCodRol(usu.getFkCodRol());
                nuevoReclamo.setFkCodEstadoReclamo(new EstadosReclamos());
                nuevoReclamo.getFkCodEstadoReclamo().setCodEstadoReclamo(1);
                nuevoReclamo.setFechaReclamo(new Date());
                nuevoReclamo.setLatitud(latituteLongitude.getLat());
                nuevoReclamo.setLongitud(latituteLongitude.getLng());
                nuevoReclamo.setDireccionReclamo(getDirReclamo());
                nuevoReclamo.setFkCodTipoReclamo(tipoDeReclamosSeleccionado);
                nuevoReclamo.setFkCodDireccion(direccionesSB.consultarDrireccionPorLatitudLongitud(latituteLongitude.getLat(), latituteLongitude.getLng()));
                nuevoReclamo.setOrigenReclamo("appWeb");
                if (reclamosSB.crearReclamos(nuevoReclamo).equals("OK")) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Gracias!", "Su reclamo fue enviado."));
                    return "admin_mis_reclamos";
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Atención!", "Ocurrio un problema al enviar su reclamo, intente de nuevo."));
                    return "admin_nuevo_reclamo";
                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Atención!", "Ocurrio un problema al enviar su reclamo, intente de nuevo."));
                return "admin_nuevo_reclamo";
            }

        }

    }

    public String enviarReclamoExterno() throws Exception {
        if (marcaParaNuevoUsuario) {
            Converciones c = new Converciones();
            String contrasenaMD5 = c.getMD5(nuevoUsuario.getFkCodPersona().getCedulaPersona());
            nuevoUsuario.setClaveUsuario(contrasenaMD5);
            nuevoUsuario.getFkCodPersona().setOrigenRegistro("appWeb_" + recuperarUsuarioSession().getFkCodRol().getNombreRol());
            nuevoUsuario.setFkCodEstadoUsuario(new EstadosUsuarios());
            nuevoUsuario.getFkCodEstadoUsuario().setCodEstadoUsuario(1);
            nuevoUsuario.setFkCodRol(new Roles());
            nuevoUsuario.getFkCodRol().setCodRol(6);
        }
        String resultado = usuariosSB.crearUsuariosExterno(nuevoUsuario);
        if (resultado.equals("OK")) {
            Usuarios usu = usuariosSB.consultarUsuarios(nuevoUsuario.getLoginUsuario());
            if (this.imagenParaGuardar != null) {
                this.nuevoReclamo.setFkImagen(new Imagenes());
                this.nuevoReclamo.setFkImagen(this.imagenParaGuardar);
            }
            if (this.tipoDeReclamosSeleccionado == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Por favor!", "Seleccione un tipo de reclamo."));
                return "admin_nuevo_reclamo_externo";
            } else if (emptyModel.getMarkers().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Por favor!", "Seleccione la ubicación de su reclamo."));
                return "admin_nuevo_reclamo_externo";
            } else if (dirReclamo.equals("")) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Por favor!", "Seleccione una ubicación valida."));
                return "admin_nuevo_reclamo_externo";
            } else if (dirReclamo.equals("")) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Por favor!", "Escriba la dirección de su reclamo."));
                return "admin_nuevo_reclamo_externo";
            } else if (this.barrioSeleccionado == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Por favor!", "Seleccione un barrio."));
                return "admin_nuevo_reclamo_externo";
            } else if (nuevoReclamo.getDescripcionReclamoContribuyente().equals("")) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Por favor!", "Detalle el reclamo."));
                return "admin_nuevo_reclamo_externo";
            } else {
                nuevoReclamo.getFkCodDireccion().setDireccionNombre(getDirReclamo());
                nuevoReclamo.getFkCodDireccion().setDireccionLatitud(latituteLongitude.getLat());
                nuevoReclamo.getFkCodDireccion().setDireccionLongitud(latituteLongitude.getLng());
                if (direccionesSB.actualizarDireccion(nuevoReclamo.getFkCodDireccion()) != null) {
                    nuevoReclamo.setFkCodUsuario(usu);
                    nuevoReclamo.setFkCodEstadoReclamo(new EstadosReclamos());
                    nuevoReclamo.getFkCodEstadoReclamo().setCodEstadoReclamo(1);
                    nuevoReclamo.setFechaReclamo(new Date());
                    nuevoReclamo.setLatitud(latituteLongitude.getLat());
                    nuevoReclamo.setLongitud(latituteLongitude.getLng());
                    nuevoReclamo.setDireccionReclamo(dirReclamo);
                    nuevoReclamo.setFkCodTipoReclamo(tipoDeReclamosSeleccionado);
                    nuevoReclamo.setFkCodDireccion(direccionesSB.consultarDrireccionPorLatitudLongitud(latituteLongitude.getLat(), latituteLongitude.getLng()));
                    nuevoReclamo.setOrigenReclamo("appWeb_" + recuperarUsuarioSession().getFkCodRol().getNombreRol());
                    String mensajeReclamo = reclamosSB.crearReclamos(nuevoReclamo);
                    if (mensajeReclamo.equals("OK")) {
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Gracias!", "Su reclamo fue enviado."));
                        return "admin_mis_reclamos";
                    } else {
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ocurrio un problema al enviar su reclamo, intente de nuevo.", mensajeReclamo));
                        return "admin_nuevo_reclamo_externo";
                    }
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Atención!", "Ocurrio un problema al enviar su reclamo, intente de nuevo."));
                    return "admin_nuevo_reclamo_externo";
                }

            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR de Registro de Usuario, intente de nuevo", resultado));
            return "/admin_nuevo_reclamo_externo";
        }

    }

    public void buscarPorCedula() {
        Personas personaBuscada = personasSB.consultarPersonaCedula(nuevoUsuario.getFkCodPersona().getCedulaPersona());
        Usuarios usuarioBuscado = new Usuarios();
        if (personaBuscada != null) {
            this.activarCamposNuevoUsuario = true;
            int banderaUsuarioBuscado = 0;
            for (int i = 0; personaBuscada.getUsuariosList().size() > i; i++) {
                if (personaBuscada.getUsuariosList().get(i).getFkCodRol().getCodRol().equals(6)) {
                    banderaUsuarioBuscado = 1;
                    usuarioBuscado = personaBuscada.getUsuariosList().get(i);
                }
            }
            if (banderaUsuarioBuscado == 1) {
                this.activarCampoCorreo = true;
                this.marcaParaNuevoUsuario = false;
            } else {
                this.activarCampoCorreo = false;
                this.marcaParaNuevoUsuario = true;
            }
            nuevoUsuario = usuarioBuscado;
            nuevoUsuario.setFkCodPersona(personaBuscada);
            if (personaBuscada.getDireccionPersona() == null || personaBuscada.getDireccionPersona().isEmpty()) {
                this.activarCampoDireccion = false;
            } else {
                this.activarCampoDireccion = true;
            }
            if (personaBuscada.getCtaCtePersona() == null || personaBuscada.getCtaCtePersona().isEmpty()) {
                this.activarCamposCuenta = false;
            } else {
                this.activarCamposCuenta = true;
            }
            if (personaBuscada.getTelefonoPersona() == null || personaBuscada.getTelefonoPersona().isEmpty()) {
                this.activarCamposTelefono = false;
            } else {
                this.activarCamposTelefono = true;
            }

        } else {
            this.marcaParaNuevoUsuario = true;
            this.activarCamposNuevoUsuario = false;
            this.activarCampoDireccion = false;
            this.activarCamposCuenta = false;
            this.activarCamposTelefono = false;
            nuevoUsuario = usuarioBuscado;
            nuevoUsuario.setFkCodPersona(new Personas());
        }
    }

    public void bucarPorCorreo() {
        Usuarios usuarioBuscado = usuariosSB.consultarUsuarios(nuevoUsuario.getLoginUsuario());
        if (usuarioBuscado != null) {
            this.mensajeCorreo = "Correo electrónico ya existe, por favor escriba otro.";
        } else {
            this.mensajeCorreo = "";
        }
    }

    public String prepararCambiarTipoReclamo(Integer codReclamo) {
        reclamoSeleccionado = reclamosSB.consultarReclamo(codReclamo);
        tipoDeReclamosAnterior = reclamoSeleccionado.getFkCodTipoReclamo();
        return "admin_cambiar_tipo_reclamo";
    }

    public String derivarReclamo() {

        reclamoSeleccionado.setFkCodUsuarioDerivacion(recuperarUsuarioSession());
        if (reclamosSB.actualizarReclamosDerivacion(reclamoSeleccionado, tipoDeReclamosAnterior).equals("OK")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Reclamo derivado.", ""));
            //METODO PARA DESCARGAR PDF DESPUES DE ACTUALIZAR RECLAMO
            return "/admin_gestion_reclamos_pendientes";
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ocurrio un error al derivar el reclamo.", ""));
            //METODO PARA DESCARGAR PDF DESPUES DE ACTUALIZAR RECLAMO
            return "/admin_cambiar_tipo_reclamo";
        }

    }

    public void onStateChange(StateChangeEvent event) {
        setZoom(event.getZoomLevel());
    }

    public Usuarios recuperarUsuarioSession() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        MbSUsuarios usuario = (MbSUsuarios) session.getAttribute("mbSUsuarios");
        return usuario.getUsuario();
    }

    public void validateFile(FacesContext ctx, UIComponent comp, Object value) {
        List<FacesMessage> msgs = new ArrayList<>();
        Part file = (Part) value;
        if (file.getSize() > 1024) {
            msgs.add(new FacesMessage("file too big"));
        }
        if (!"text/plain".equals(file.getContentType())) {
            msgs.add(new FacesMessage("not a text file"));
        }
        if (!msgs.isEmpty()) {
            throw new ValidatorException(msgs);
        }
    }

    public void cargarImagen(FileUploadEvent event) throws IOException {
        UploadedFile file = event.getFile();
//        BufferedImage src = ImageIO.read(file.getInputstream());
//        int valor1 = 1024;
//        int valor2 = 768;
//        int nAlto;
//        int nAncho;
//        if (src.getHeight() > src.getWidth()) {
//            nAlto = valor1;
//            nAncho = valor2;
//        } else if (src.getHeight() < src.getWidth()) {
//            nAlto = valor2;
//            nAncho = valor1;
//        } else {
//            nAlto = valor2;
//            nAncho = valor2;
//        }
        try {
            //Se obtine la imagen que se va a guardar en la Base de datos
            this.imagenParaGuardar = new Imagenes();
            //this.imagenParaGuardar.setArchivoImagen(resize(file.getInputstream(), nAncho, nAlto));
            this.imagenParaGuardar.setArchivoImagen(IOUtils.toByteArray(file.getInputstream()));
            this.imagenParaGuardar.setTipoImagen(file.getContentType());
            this.imagenParaGuardar.setNombreImagen(file.getFileName());
            //Se convierte la imagen obtenida para mostrar como previa
            this.imagenCargada = null;
            this.imagenCargada = new DefaultStreamedContent(new ByteArrayInputStream(this.imagenParaGuardar.getArchivoImagen()), this.imagenParaGuardar.getTipoImagen());
            this.imagenCargada.setName(this.imagenParaGuardar.getNombreImagen());
            this.imagenCargada.setContentType(this.imagenParaGuardar.getTipoImagen());
            this.setMostrarGraphicImage(true);

        } catch (Exception ex) {
            Logger.getLogger(MbSReclamos.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void cancelarImagenCargada() {
        this.setMostrarGraphicImage(false);
        this.imagenParaGuardar = null;
        this.imagenCargada = null;
    }

    private byte[] ajustarImagen(InputStream imagen, int IMG_WIDTH, int IMG_HEIGHT, String tipoImagen) throws Exception {
        String tipo = tipoImagen.substring(6, tipoImagen.length());
        // InputStream inputStream = new ByteArrayInputStream(imagen);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            BufferedImage originalImage = ImageIO.read(imagen);
            int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
            BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
            g.dispose();
            g.setComposite(AlphaComposite.Src);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ImageIO.write(resizedImage, tipo, baos);
        } catch (Throwable ex) {
            throw new Exception("Error proceso Tamaño Imagen " + ex.toString(), ex);
        }
        return baos.toByteArray();
    }

    public byte[] resize(InputStream input, int width, int height) throws Exception {
        BufferedImage src = ImageIO.read(input);
        BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dest.createGraphics();
        AffineTransform at = AffineTransform.getScaleInstance((double) width / src.getWidth(), (double) height / src.getHeight());
        g.drawRenderedImage(src, at);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(dest, "JPG", output);
        return output.toByteArray();
    }

    public String formatearFecha(Date fecha) {
        // formateo de fechas
        String patron = "dd-MM-yyyy";
        SimpleDateFormat formato = new SimpleDateFormat(patron);
        if (fecha == null) {
            return "";
        } else {
            return formato.format(fecha);
        }
    }

    public int tiempoTranscurrido(Reclamos reclamo) {
        //Se crean objetos calendario con la fecha actual
        Calendar hoy = Calendar.getInstance();
        Calendar c = Calendar.getInstance();
        int dias = 0;
        int ban = 0;
        int diasMaximo = 0;
        //Se crea un objeto calendario con la fecha del inicio del reclamo
        Calendar fechaInicio = new GregorianCalendar();
        if (reclamo.getFkCodEstadoReclamo().getCodEstadoReclamo().equals(1)) {
            //PARA ESTADO PENDIENTE
            fechaInicio.setTime(reclamo.getFechaReclamo());
            diasMaximo = reclamo.getFkCodTipoReclamo().getDiasMaximoPendientes();
            ban = 1;
        } else if (reclamo.getFkCodEstadoReclamo().getCodEstadoReclamo().equals(2)) {
            //PARA ESTADO ATENDIDO
            fechaInicio.setTime(reclamo.getFechaAtencionReclamo());
            diasMaximo = reclamo.getFkCodTipoReclamo().getDiasMaximoFinalizados();
            ban = 1;
        } else if (reclamo.getFkCodEstadoReclamo().getCodEstadoReclamo().equals(3)) {
            //PARA ESTADO FINALIZADO
            diasMaximo = reclamo.getCantidadDiasProceso();
        }
        if (ban == 1) {
            //Obtiene el dia
            c.setTimeInMillis(hoy.getTime().getTime() - fechaInicio.getTime().getTime());
            dias = c.get(Calendar.DAY_OF_YEAR);

        }
        mostrarSemaforo(dias, diasMaximo);
        if (ban == 0) {
            dias = diasMaximo;
        }
        return dias;
    }

    public void mostrarSemaforo(Integer dias, Integer diasMaximo) {
        if (dias == 0) {
            setImagenSemaforo(null);
        } else if (dias < diasMaximo) {
            setImagenSemaforo("verde20.jpg");
        } else if (dias >= diasMaximo && dias < diasMaximo) {
            setImagenSemaforo("amarillo20.jpg");
        } else if (dias >= diasMaximo) {
            setImagenSemaforo("rojo20.gif");
        }
    }

    public void exportarPDF(Integer codReclamo, String modo) throws JRException, IOException, Exception {
        //Se inicializa con las configuraciones para el uso de mapas de Google
        setConfiguraciones(configuracionesSB.consultarPorCodConfiguracion(2));

        Reclamos reclamoSeleccionadoPDF = reclamosSB.consultarReclamo(codReclamo);
        JasperReport jasper;
        Map<String, Object> parametros = new HashMap<>();
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        String urlImagen = ((ServletContext) ctx.getContext()).getRealPath("/resources/images/escudo.gif");
        String urlImagen2 = ((ServletContext) ctx.getContext()).getRealPath("/resources/images/asu_logo_report.png");

        parametros.put("urlImagen", urlImagen);
        parametros.put("urlImagen2", urlImagen2);
        parametros.put("nombreDependencia", reclamoSeleccionadoPDF.getFkCodTipoReclamo().getFkCodDependencia().getNombreDependencia());
        parametros.put("nombreTipoReclamo", reclamoSeleccionadoPDF.getFkCodTipoReclamo().getNombreTipoReclamo());
        parametros.put("cedulaPersona", reclamoSeleccionadoPDF.getFkCodUsuario().getFkCodPersona().getCedulaPersona());
        parametros.put("nombrePersona", reclamoSeleccionadoPDF.getFkCodUsuario().getFkCodPersona().getNombrePersona());
        parametros.put("apellidoPersona", reclamoSeleccionadoPDF.getFkCodUsuario().getFkCodPersona().getApellidoPersona());
        parametros.put("direccionPersona", reclamoSeleccionadoPDF.getFkCodUsuario().getFkCodPersona().getDireccionPersona());
        parametros.put("telefonoPersona", reclamoSeleccionadoPDF.getFkCodUsuario().getFkCodPersona().getTelefonoPersona());

        parametros.put("codReclamo", reclamoSeleccionadoPDF.getCodReclamo());
        parametros.put("fechaReclamo", reclamoSeleccionadoPDF.getFechaReclamo());
        parametros.put("direccionReclamo", reclamoSeleccionadoPDF.getDireccionReclamo());
        parametros.put("latitud", reclamoSeleccionadoPDF.getLatitud());
        parametros.put("longitud", reclamoSeleccionadoPDF.getLongitud());
        parametros.put("direccionReclamo", reclamoSeleccionadoPDF.getDireccionReclamo());
        parametros.put("descripcionReclamoContribuyente", reclamoSeleccionadoPDF.getDescripcionReclamoContribuyente());
        parametros.put("estadoReclamo", reclamoSeleccionadoPDF.getFkCodEstadoReclamo().getNombreEstadoReclamo());

        String latLon = reclamoSeleccionadoPDF.getLatitud() + "," + reclamoSeleccionadoPDF.getLongitud();
        String urlMapa = getConfiguraciones().getPar02() + getConfiguraciones().getPar03() + getConfiguraciones().getPar01()
                + "&center=" + latLon
                + "&markers=" + latLon;

        parametros.put("urlMapa", urlMapa);

        if (reclamoSeleccionadoPDF.getFkImagen() == null
                || reclamoSeleccionadoPDF.getFkImagen().getArchivoImagen() == null
                || reclamoSeleccionadoPDF.getFkImagen().getArchivoImagen().equals("")
                || reclamoSeleccionadoPDF.getFkImagen().getNombreImagen() == null
                || reclamoSeleccionadoPDF.getFkImagen().getNombreImagen().equals("")) {
            String urlImagen3 = ((ServletContext) ctx.getContext()).getRealPath("/resources/images/blanco.png");
            File imageFile = new File(urlImagen3);
            InputStream is = new FileInputStream(imageFile);
            parametros.put("imagenReclamo", ajustarImagen(is, 640, 480, "image/png"));
            System.out.println("SIN IMAGEN");
        } else {
            parametros.put("imagenReclamo", reclamoSeleccionadoPDF.getFkImagen().getArchivoImagen());
            System.out.println("CON IMAGEN");
        }

        //Se verifica estado del reclamo. codEstadoReclamo = 1 --> PENDIENTE
        if (reclamoSeleccionadoPDF.getFkCodEstadoReclamo().getCodEstadoReclamo().equals(1)) {
            if (modo.equals("MIS_RECLAMOS")) { //Se verifica el modo para generar. modo = MIS_RECLAMOS o modo = GESTION
                jasper = (JasperReport) JRLoader.loadObject(getClass().getClassLoader().getResourceAsStream("py/gov/mca/reclamosmca/reportes/ReclamoPendienteCiudadano.jasper"));
            } else {
                jasper = (JasperReport) JRLoader.loadObject(getClass().getClassLoader().getResourceAsStream("py/gov/mca/reclamosmca/reportes/ReclamoPendienteGestion.jasper"));
            }
            //Se verifica que estado tiene el reclamo. codEstadoReclamo = 2 --> ATENDIDO, codEstadoReclamo = 3 --> FINALIZADO
        } else if (reclamoSeleccionadoPDF.getFkCodEstadoReclamo().getCodEstadoReclamo().equals(2) || reclamoSeleccionadoPDF.getFkCodEstadoReclamo().getCodEstadoReclamo().equals(3)) {
            parametros.put("fechaAtencion", reclamoSeleccionadoPDF.getFechaAtencionReclamo());
            parametros.put("usuarioAtencion", reclamoSeleccionadoPDF.getFkCodUsuarioAtencion().getFkCodPersona().getNombrePersona() + " " + reclamoSeleccionadoPDF.getFkCodUsuarioAtencion().getFkCodPersona().getApellidoPersona());
            parametros.put("descripcionAtencion", reclamoSeleccionadoPDF.getDescripcionAtencionReclamo());
            if (modo.equals("MIS_RECLAMOS")) { //Se verifica el modo para generar. modo = MIS_RECLAMOS o modo = GESTION
                jasper = (JasperReport) JRLoader.loadObject(getClass().getClassLoader().getResourceAsStream("py/gov/mca/reclamosmca/reportes/ReclamoAtendidoCiudadano.jasper"));
            } else {
                jasper = (JasperReport) JRLoader.loadObject(getClass().getClassLoader().getResourceAsStream("py/gov/mca/reclamosmca/reportes/ReclamoAtendidoGestion.jasper"));
            }
            //Se verifica estado del reclamo. codEstadoReclamo = 3 --> FINALIZADO
            if (reclamoSeleccionadoPDF.getFkCodEstadoReclamo().getCodEstadoReclamo().equals(3)) {
                parametros.put("fechaFinalizacion", reclamoSeleccionadoPDF.getFechaCulminacionReclamo());
                parametros.put("usuarioFinalizacion", reclamoSeleccionadoPDF.getFkCodUsuarioCulminacion().getFkCodPersona().getNombrePersona() + " " + reclamoSeleccionadoPDF.getFkCodUsuarioCulminacion().getFkCodPersona().getApellidoPersona());
                parametros.put("descripcionFinalizacion", reclamoSeleccionadoPDF.getDescripcionCulminacionReclamo());
                if (modo.equals("MIS_RECLAMOS")) { //Se verifica el modo para generar. modo = MIS_RECLAMOS o modo = GESTION
                    jasper = (JasperReport) JRLoader.loadObject(getClass().getClassLoader().getResourceAsStream("py/gov/mca/reclamosmca/reportes/ReclamoFinalizadoCiudadano.jasper"));
                } else {
                    jasper = (JasperReport) JRLoader.loadObject(getClass().getClassLoader().getResourceAsStream("py/gov/mca/reclamosmca/reportes/ReclamoFinalizadoGestion.jasper"));
                }
            }
        } else {
            jasper = null;
        }

        // File jasper = new File(FacesContext.getCurrentInstance().getExternalContext().getRealPath("/src/java/py/gov/mca/reclamosmca/reportes/mapas.jasper"));
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasper, parametros, new JREmptyDataSource());
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("application/pdf");
        response.addHeader("Content-disposition", "attachment; filename=RECLAMO_" + reclamoSeleccionadoPDF.getCodReclamo() + "_" + reclamoSeleccionadoPDF.getFkCodEstadoReclamo().getNombreEstadoReclamo() + ".pdf");
        //response.
        //Response.Write("<script>window.print();</script>"); 

        ServletOutputStream stream = response.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, stream);

        stream.flush();
        stream.close();
        FacesContext.getCurrentInstance().responseComplete();

    }

    public String verDetalleReclamo(Integer codReclamo, String modo) {
        reclamoSeleccionado = reclamosSB.consultarReclamo(codReclamo);
        String retorno;
        switch (modo) {
            case "PENDIENTE":
                retorno = "admin_ver_detalle_reclamo_pendiente";
                break;
            case "ATENCION":
                retorno = "admin_ver_detalle_reclamo_atendido";
                break;
            default:
                retorno = "admin_ver_detalle_reclamo_finalizado";
                break;
        }

        return retorno;
    }

    public void exportarPDFporRangoFechaDependencia() throws JRException, IOException {
        JasperReport jasper;
        Usuarios usu = recuperarUsuarioSession();
        List<Reclamos> lista = reclamosSB.listarPorDependenciaRangoDeFecha(usu.getFkCodPersona().getFkCodDependencia().getCodDependencia(), getFechaInicio(), getFechaFin());
        int cantidadReclamos = lista.size();
        int cantidadSinAtender = 0;
        int cantidadAtendidos = 0;
        int cantidadFinalizados = 0;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getFkCodEstadoReclamo().getCodEstadoReclamo() == 1) {
                cantidadSinAtender = cantidadSinAtender + 1;
            } else if (lista.get(i).getFkCodEstadoReclamo().getCodEstadoReclamo() == 2) {
                cantidadAtendidos = cantidadAtendidos + 1;
            } else if (lista.get(i).getFkCodEstadoReclamo().getCodEstadoReclamo() == 3) {
                cantidadFinalizados = cantidadFinalizados + 1;
            }
        }

        Map<String, Object> parametros = new HashMap<>();
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        String urlImagen = ((ServletContext) ctx.getContext()).getRealPath("/resources/images/escudo.gif");
        String urlImagen2 = ((ServletContext) ctx.getContext()).getRealPath("/resources/images/asu_logo_report.png");

        parametros.put("urlImagen", urlImagen);
        parametros.put("urlImagen2", urlImagen2);
        parametros.put("nombreDependencia", usu.getFkCodPersona().getFkCodDependencia().getNombreDependencia());
        parametros.put("fechaDesde", getFechaInicio());
        parametros.put("fechaHasta", getFechaFin());
        parametros.put("fechaGeneracion", new Date());
        parametros.put("totalReclamos", cantidadReclamos);
        parametros.put("totalReclamosSinAtender", cantidadSinAtender);
        parametros.put("totalReclamosAtendidos", cantidadAtendidos);
        parametros.put("totalReclamosFinalizados", cantidadFinalizados);
        parametros.put("usuarioGeneracion", usu.getFkCodPersona().getNombrePersona() + " " + usu.getFkCodPersona().getApellidoPersona());

        jasper = (JasperReport) JRLoader.loadObject(getClass().getClassLoader().getResourceAsStream("py/gov/mca/reclamosmca/reportes/ReclamoRangoFechaDependenciaReporte.jasper"));

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasper, parametros, new JREmptyDataSource());
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("application/pdf");
        response.addHeader("Content-disposition", "attachment; filename=REPORTE_RANGO_FECHA_DEPENDENCIA.pdf");
        //response.
        //Response.Write("<script>window.print();</script>"); 

        ServletOutputStream stream = response.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, stream);

        stream.flush();
        stream.close();
        FacesContext.getCurrentInstance().responseComplete();
    }

    public void exportarPDFporRangoFechaDependenciaTiposReclamos() throws JRException, IOException {
        JasperReport jasper;
        Usuarios usu = recuperarUsuarioSession();
        List<Reclamos> listaReclamos = reclamosSB.listarPorDependenciaRangoDeFecha(usu.getFkCodPersona().getFkCodDependencia().getCodDependencia(), getFechaInicio(), getFechaFin());
        List<TiposReclamosCantidad> listaTiposReclamosCantidad = new ArrayList<>();

        for (int i = 0; listaReclamos.size() > i; i++) {
            TiposReclamosCantidad tiposReclamosCantidad = new TiposReclamosCantidad();
            tiposReclamosCantidad.setNombreTipoReclamo(listaReclamos.get(i).getFkCodTipoReclamo().getNombreTipoReclamo());
            tiposReclamosCantidad.setCantidadTipoReclamo(0);
            for (int j = 0; listaReclamos.size() > j; j++) {
                if (listaReclamos.get(i).getFkCodTipoReclamo().getCodTipoReclamo().equals(listaReclamos.get(j).getFkCodTipoReclamo().getCodTipoReclamo())) {
                    tiposReclamosCantidad.setCantidadTipoReclamo(tiposReclamosCantidad.getCantidadTipoReclamo() + 1);
                }
            }
            listaTiposReclamosCantidad.add(tiposReclamosCantidad);
        }

        Map<String, Object> parametros = new HashMap<>();
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        String urlImagen = ((ServletContext) ctx.getContext()).getRealPath("/resources/images/escudo.gif");
        String urlImagen2 = ((ServletContext) ctx.getContext()).getRealPath("/resources/images/asu_logo_report.png");

        parametros.put("urlImagen", urlImagen);
        parametros.put("urlImagen2", urlImagen2);
        parametros.put("nombreDependencia", usu.getFkCodPersona().getFkCodDependencia().getNombreDependencia());
        parametros.put("fechaDesde", getFechaInicio());
        parametros.put("fechaHasta", getFechaFin());
        parametros.put("fechaGeneracion", new Date());
        parametros.put("totalReclamos", listaReclamos.size());
        parametros.put("usuarioGeneracion", usu.getFkCodPersona().getNombrePersona() + " " + usu.getFkCodPersona().getApellidoPersona());

        JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(listaTiposReclamosCantidad);
        jasper = (JasperReport) JRLoader.loadObject(getClass().getClassLoader().getResourceAsStream("py/gov/mca/reclamosmca/reportes/ReclamoRangoFechaDependenciaTiposReclamosReporte.jasper"));

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasper, parametros, beanCollectionDataSource);

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("application/pdf");
        response.addHeader("Content-disposition", "attachment; filename=REPORTE_RANGO_FECHA_DEPENDENCIA_TIPOS_RECLAMOS.pdf");
        //response.
        //Response.Write("<script>window.print();</script>"); 

        ServletOutputStream stream = response.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, stream);

        stream.flush();
        stream.close();
        FacesContext.getCurrentInstance().responseComplete();

    }

    public void exportarPDFporRangoFechaEstadoReclamos() throws JRException, IOException {
        JasperReport jasper;
        Usuarios usu = recuperarUsuarioSession();
        List<Reclamos> listaReclamos = reclamosSB.listarDependenciaTipoReclamosEstadoReclamoRangoFecha(this.codigoEstadoReclamo, this.fechaInicio, this.fechaFin);

        List<DependenciasReporte> listaDependenciasReporte = new ArrayList<>();

        DependenciasReporte dependenciasReporte = new DependenciasReporte();
        dependenciasReporte.setCodDependencia(0);
        //Se buscan las dependencias
        for (int i = 0; listaReclamos.size() > i; i++) {
            if (!dependenciasReporte.getCodDependencia().equals(listaReclamos.get(i).getFkCodTipoReclamo().getFkCodDependencia().getCodDependencia())) {
                dependenciasReporte = new DependenciasReporte();
                dependenciasReporte.setCodDependencia(listaReclamos.get(i).getFkCodTipoReclamo().getFkCodDependencia().getCodDependencia());
                dependenciasReporte.setNombreDependencia(listaReclamos.get(i).getFkCodTipoReclamo().getFkCodDependencia().getNombreDependencia());
                dependenciasReporte.setCantidadReclamosDependencia(0);
                dependenciasReporte.setTiposReclamosReporte(new ArrayList<>());
                listaDependenciasReporte.add(dependenciasReporte);
            }
        }
        //Se buscan los tipos de reclamos
        int ban = 0;
        for (int i = 0; listaReclamos.size() > i; i++) {
            for (int j = 0; listaDependenciasReporte.size() > j; j++) {
                if (listaDependenciasReporte.get(j).getCodDependencia().equals(listaReclamos.get(i).getFkCodTipoReclamo().getFkCodDependencia().getCodDependencia())) {
                    if (ban != listaReclamos.get(i).getFkCodTipoReclamo().getCodTipoReclamo()) {
                        TiposReclamosReporte tiposReclamosReporte;
                        tiposReclamosReporte = new TiposReclamosReporte();
                        tiposReclamosReporte.setCodTipoReclamo(listaReclamos.get(i).getFkCodTipoReclamo().getCodTipoReclamo());
                        tiposReclamosReporte.setNombreTipoReclamo(listaReclamos.get(i).getFkCodTipoReclamo().getNombreTipoReclamo());
                        tiposReclamosReporte.setCatidadReclamosTipos(0);
                        tiposReclamosReporte.setReclamos(new ArrayList<>());
                        listaDependenciasReporte.get(j).getTiposReclamosReporte().add(tiposReclamosReporte);
                        ban = listaReclamos.get(i).getFkCodTipoReclamo().getCodTipoReclamo();
                    }
                }
            }
        }
        //Se buscan los reclamos
        for (int i = 0; listaReclamos.size() > i; i++) {
            for (int j = 0; listaDependenciasReporte.size() > j; j++) {
                if (listaDependenciasReporte.get(j).getCodDependencia().equals(listaReclamos.get(i).getFkCodTipoReclamo().getFkCodDependencia().getCodDependencia())) {
                    for (int k = 0; listaDependenciasReporte.get(j).getTiposReclamosReporte().size() > k; k++) {
                        if (listaDependenciasReporte.get(j).getTiposReclamosReporte().get(k).getCodTipoReclamo().equals(listaReclamos.get(i).getFkCodTipoReclamo().getCodTipoReclamo())) {
                            listaDependenciasReporte.get(j).getTiposReclamosReporte().get(k).getReclamos().add(listaReclamos.get(i));
                            listaDependenciasReporte.get(j).getTiposReclamosReporte().get(k).setCatidadReclamosTipos(listaDependenciasReporte.get(j).getTiposReclamosReporte().get(k).getCatidadReclamosTipos() + 1);
                            listaDependenciasReporte.get(j).setCantidadReclamosDependencia(listaDependenciasReporte.get(j).getCantidadReclamosDependencia() + 1);
                        }
                    }
                }
            }
        }

        Map<String, Object> parametros = new HashMap<>();
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        String urlImagen = ((ServletContext) ctx.getContext()).getRealPath("/resources/images/escudo.gif");
        String urlImagen2 = ((ServletContext) ctx.getContext()).getRealPath("/resources/images/asu_logo_report.png");

        parametros.put("urlImagen", urlImagen);
        parametros.put("urlImagen2", urlImagen2);
        parametros.put("nombreDependencia", usu.getFkCodPersona().getFkCodDependencia().getNombreDependencia());
        parametros.put("fechaDesde", getFechaInicio());
        parametros.put("fechaHasta", getFechaFin());
        parametros.put("fechaGeneracion", new Date());
        parametros.put("totalReclamos", listaReclamos.size());
        parametros.put("usuarioGeneracion", usu.getFkCodPersona().getNombrePersona() + " " + usu.getFkCodPersona().getApellidoPersona());
        parametros.put("SUBREPORT_DIR", "py/gov/mca/reclamosmca/reportes/");

        JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(listaDependenciasReporte);
        jasper = (JasperReport) JRLoader.loadObject(getClass().getClassLoader().getResourceAsStream("py/gov/mca/reclamosmca/reportes/ReclamoRangoFechaEstadoReclamos.jasper"));

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasper, parametros, beanCollectionDataSource);

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("application/pdf");
        response.addHeader("Content-disposition", "attachment; filename=REPORTE_RANGO_FECHA_TIPOS_RECLAMOS.pdf");
        //response.
        //Response.Write("<script>window.print();</script>"); 

        ServletOutputStream stream = response.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, stream);

        stream.flush();
        stream.close();
        FacesContext.getCurrentInstance().responseComplete();
    }

    public void exportarPDFporDependenciaRangoFechaEstadoReclamos() throws JRException, IOException {
        JasperReport jasper;
        Usuarios usu = recuperarUsuarioSession();
        List<Reclamos> listaReclamos = reclamosSB.listarPorDependenciaRangoDeFecha(usu.getFkCodPersona().getFkCodDependencia().getCodDependencia(), getFechaInicio(), getFechaFin());

        List<DependenciasReporte> listaDependenciasReporte = new ArrayList<>();

        DependenciasReporte dependenciasReporte = new DependenciasReporte();
        dependenciasReporte.setCodDependencia(0);
        //Se buscan las dependencias
        for (int i = 0; listaReclamos.size() > i; i++) {
            if (!dependenciasReporte.getCodDependencia().equals(listaReclamos.get(i).getFkCodTipoReclamo().getFkCodDependencia().getCodDependencia())) {
                dependenciasReporte = new DependenciasReporte();
                dependenciasReporte.setCodDependencia(listaReclamos.get(i).getFkCodTipoReclamo().getFkCodDependencia().getCodDependencia());
                dependenciasReporte.setNombreDependencia(listaReclamos.get(i).getFkCodTipoReclamo().getFkCodDependencia().getNombreDependencia());
                dependenciasReporte.setCantidadReclamosDependencia(0);
                dependenciasReporte.setTiposReclamosReporte(new ArrayList<>());
                listaDependenciasReporte.add(dependenciasReporte);
            }
        }
        //Se buscan los tipos de reclamos
        int ban = 0;
        for (int i = 0; listaReclamos.size() > i; i++) {
            for (int j = 0; listaDependenciasReporte.size() > j; j++) {
                if (listaDependenciasReporte.get(j).getCodDependencia().equals(listaReclamos.get(i).getFkCodTipoReclamo().getFkCodDependencia().getCodDependencia())) {
                    if (ban != listaReclamos.get(i).getFkCodTipoReclamo().getCodTipoReclamo()) {
                        TiposReclamosReporte tiposReclamosReporte;
                        tiposReclamosReporte = new TiposReclamosReporte();
                        tiposReclamosReporte.setCodTipoReclamo(listaReclamos.get(i).getFkCodTipoReclamo().getCodTipoReclamo());
                        tiposReclamosReporte.setNombreTipoReclamo(listaReclamos.get(i).getFkCodTipoReclamo().getNombreTipoReclamo());
                        tiposReclamosReporte.setCatidadReclamosTipos(0);
                        tiposReclamosReporte.setReclamos(new ArrayList<>());
                        listaDependenciasReporte.get(j).getTiposReclamosReporte().add(tiposReclamosReporte);
                        ban = listaReclamos.get(i).getFkCodTipoReclamo().getCodTipoReclamo();
                    }
                }
            }
        }
        //Se buscan los reclamos
        for (int i = 0; listaReclamos.size() > i; i++) {
            for (int j = 0; listaDependenciasReporte.size() > j; j++) {
                if (listaDependenciasReporte.get(j).getCodDependencia().equals(listaReclamos.get(i).getFkCodTipoReclamo().getFkCodDependencia().getCodDependencia())) {
                    for (int k = 0; listaDependenciasReporte.get(j).getTiposReclamosReporte().size() > k; k++) {
                        if (listaDependenciasReporte.get(j).getTiposReclamosReporte().get(k).getCodTipoReclamo().equals(listaReclamos.get(i).getFkCodTipoReclamo().getCodTipoReclamo())) {
                            listaDependenciasReporte.get(j).getTiposReclamosReporte().get(k).getReclamos().add(listaReclamos.get(i));
                            listaDependenciasReporte.get(j).getTiposReclamosReporte().get(k).setCatidadReclamosTipos(listaDependenciasReporte.get(j).getTiposReclamosReporte().get(k).getCatidadReclamosTipos() + 1);
                            listaDependenciasReporte.get(j).setCantidadReclamosDependencia(listaDependenciasReporte.get(j).getCantidadReclamosDependencia() + 1);
                        }
                    }
                }
            }
        }

        Map<String, Object> parametros = new HashMap<>();
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        String urlImagen = ((ServletContext) ctx.getContext()).getRealPath("/resources/images/escudo.gif");
        String urlImagen2 = ((ServletContext) ctx.getContext()).getRealPath("/resources/images/asu_logo_report.png");

        parametros.put("urlImagen", urlImagen);
        parametros.put("urlImagen2", urlImagen2);
        parametros.put("nombreDependencia", usu.getFkCodPersona().getFkCodDependencia().getNombreDependencia());
        parametros.put("fechaDesde", getFechaInicio());
        parametros.put("fechaHasta", getFechaFin());
        parametros.put("fechaGeneracion", new Date());
        parametros.put("totalReclamos", listaReclamos.size());
        parametros.put("usuarioGeneracion", usu.getFkCodPersona().getNombrePersona() + " " + usu.getFkCodPersona().getApellidoPersona());
        parametros.put("SUBREPORT_DIR", "py/gov/mca/reclamosmca/reportes/");

        JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(listaDependenciasReporte);
        jasper = (JasperReport) JRLoader.loadObject(getClass().getClassLoader().getResourceAsStream("py/gov/mca/reclamosmca/reportes/ReclamoRangoFechaEstadoReclamosPorDependencia.jasper"));

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasper, parametros, beanCollectionDataSource);

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("application/pdf");
        response.addHeader("Content-disposition", "attachment; filename=REPORTE_TIPOS_RECLAMOS_ESTADO_RANGO_FECHA.pdf");
        //response.
        //Response.Write("<script>window.print();</script>"); 

        ServletOutputStream stream = response.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, stream);

        stream.flush();
        stream.close();
        FacesContext.getCurrentInstance().responseComplete();
    }

    public Integer cantidadReclamosPorZona(Reclamos reclamo) {
        List<Reclamos> lista1 = reclamosSB.listarPorTiposReclamos(reclamo.getFkCodTipoReclamo().getCodTipoReclamo());
        List<Reclamos> lista2 = new ArrayList<>();
        for (Reclamos reclamoAux : lista1) {

            if (!reclamoAux.getFkCodEstadoReclamo().getCodEstadoReclamo().equals(3) && !reclamo.getCodReclamo().equals(reclamoAux.getCodReclamo())) {
                //CONTROL PARA TIEMPO DE RECLAMOS SIMILARES
                if (cantidadDias(reclamoAux.getFechaReclamo()) < 31) {
                    double distancia = distanciaEntrePuntos(reclamo.getLatitud(), reclamo.getLongitud(), reclamoAux.getLatitud(), reclamoAux.getLongitud());
                    if (distancia < 20) {
                        lista2.add(reclamoAux);
                    }
                }
            }
        }
        return lista2.size();
        //listarReclamosPorZona = new ListDataModel(lista2);
    }

    public String verReclamosPorZona(Reclamos reclamo) {
        List<Reclamos> lista1 = reclamosSB.listarPorTiposReclamos(reclamo.getFkCodTipoReclamo().getCodTipoReclamo());
        List<Reclamos> lista2 = new ArrayList<>();
        for (Reclamos reclamoAux : lista1) {
            if (!reclamoAux.getFkCodEstadoReclamo().getCodEstadoReclamo().equals(3) && !reclamo.getCodReclamo().equals(reclamoAux.getCodReclamo())) {
                //CONTROL PARA TIEMPO DE RECLAMOS SIMILARES
                if (cantidadDias(reclamoAux.getFechaReclamo()) < 31) {
                    double distancia = distanciaEntrePuntos(reclamo.getLatitud(), reclamo.getLongitud(), reclamoAux.getLatitud(), reclamoAux.getLongitud());
                    if (distancia < 20) {
                        lista2.add(reclamoAux);
                    }
                }
            }
        }
        reclamosPorZona = lista2;
        return "admin_gestion_reclamos_zona";
    }

    private double distanciaEntrePuntos(double lat1, double lon1, double lat2, double lon2) {
        // Formula de Haversine para obtener la distancia entre 
        // dos puntos geográficos (longitud y latitud)
        // Radio de la Tierra: 6378 km.
        // R = earth’s radius (mean radius = 6,378km)
        // Δlat = lat2− lat1
        // Δlong = long2− long1
        // a = sin²(Δlat/2) + cos(lat1).cos(lat2).sin²(Δlong/2)
        // c = 2.atan2(√a, √(1−a))
        // d = R.c
        double R = 6378; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c * 1000;
        return d;
    }

    public void verMapa(Reclamos reclamo) {
        emptyModel = new DefaultMapModel();
        emptyModel.addOverlay(null);
        LatLng latiLongi = new LatLng(reclamo.getLatitud(), reclamo.getLongitud());
        Marker marca = new Marker(latiLongi);
        marca.setTitle(reclamo.getFkCodTipoReclamo().getNombreTipoReclamo());
        marca.setDraggable(false);
        emptyModel.addOverlay(marca);
        this.nuevoReclamo = reclamo;
    }

    public void verImagen(Reclamos reclamo) {
        reclamoSeleccionado = reclamo;
        //Se convierte la imagen obtenida para mostrar como previa
        this.imagenCargada = null;
        if (reclamoSeleccionado.getFkImagen() != null) {
            this.imagenCargada = new DefaultStreamedContent(new ByteArrayInputStream(reclamoSeleccionado.getFkImagen().getArchivoImagen()), reclamoSeleccionado.getFkImagen().getTipoImagen());
            this.imagenCargada.setName(reclamoSeleccionado.getFkImagen().getNombreImagen());
            this.imagenCargada.setContentType(reclamoSeleccionado.getFkImagen().getTipoImagen());
        }
    }

    public String recuperarReclamo(Integer codReclamo) {
        reclamoSeleccionado = null;
        reclamoSeleccionado = reclamosSB.consultarReclamo(codReclamo);
        if (reclamoSeleccionado.getFkCodDireccion().getFkCodBarrio() == null) {
            reclamoSeleccionado.getFkCodDireccion().setFkCodBarrio(new Paises04Barrios());
            reclamoSeleccionado.getFkCodDireccion().getFkCodBarrio().setCodBarrio(69);
        }
        String pagina;
        verMapa(reclamoSeleccionado);
        //Si el estado del reclamo es PENDIENTE
        if (reclamoSeleccionado.getFkCodEstadoReclamo().getCodEstadoReclamo().equals(1)) {
            pagina = "/admin_procesar_reclamo_pendiente";
            //Si el estado del reclamo es ATENDIDO
        } else {
            reclamoSeleccionado.setFkCodTipoFinalizacionReclamo(new TiposFinalizacionReclamos());
            reclamoSeleccionado.getFkCodTipoFinalizacionReclamo().setCodTipoFinalizacionReclamo(0);
            pagina = "/admin_procesar_reclamo_atendido";
        }
        return pagina;
    }

    public String actualizarReclamoPendiente() {
        //VALIDAR DIR VACIO y BARRIO NULL
        if (reclamoSeleccionado.getFkCodDireccion().getFkCodBarrio().getCodBarrio().equals(69)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Debe completar seleccionar un barrio.", ""));
            return "/admin_procesar_reclamo_pendiente";
        } else if (reclamoSeleccionado.getDescripcionAtencionReclamo().equals("") || reclamoSeleccionado.getDescripcionAtencionReclamo().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Debe completar el campo Descripción de tarea a realizar.", ""));
            return "/admin_procesar_reclamo_pendiente";
        } else {
            Paises05Direcciones dirAux = direccionesSB.consultarDrireccionPorLatitudLongitud(reclamoSeleccionado.getLatitud(), reclamoSeleccionado.getLongitud());
            if (dirAux == null) {
                dirAux = new Paises05Direcciones();
            }
            dirAux.setDireccionLatitud(reclamoSeleccionado.getLatitud());
            dirAux.setDireccionLongitud(reclamoSeleccionado.getLongitud());
            dirAux.setDireccionNombre(reclamoSeleccionado.getDireccionReclamo());
            dirAux.setFkCodBarrio(barriosSB.consultarBarrio(reclamoSeleccionado.getFkCodDireccion().getFkCodBarrio().getCodBarrio()));

            if (direccionesSB.actualizarDireccion(dirAux) != null) {
                //Se completa el reclamo
                reclamoSeleccionado.setFkCodEstadoReclamo(new EstadosReclamos());
                reclamoSeleccionado.getFkCodEstadoReclamo().setCodEstadoReclamo(2);
                // reclamoSeleccionado.getFkCodEstadoReclamo().setNombreEstadoReclamo("EN_PROCESO");
                reclamoSeleccionado.setFkCodUsuarioAtencion(new Usuarios());
                reclamoSeleccionado.setFkCodUsuarioAtencion(recuperarUsuarioSession());
                reclamoSeleccionado.setFkCodDireccion(direccionesSB.consultarDrireccionPorLatitudLongitud(reclamoSeleccionado.getLatitud(), reclamoSeleccionado.getLongitud()));
                reclamoSeleccionado.setFechaAtencionReclamo(new Date());
                reclamoSeleccionado.setFkCodTipoFinalizacionReclamo(null);
                System.err.println(reclamoSeleccionado.toString());
                String mensaje = reclamosSB.actualizarReclamos(reclamoSeleccionado);
                if (mensaje.equals("OK")) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Reclamo procesado.", ""));
                    //METODO PARA DESCARGAR PDF DESPUES DE ACTUALIZAR RECLAMO
                    return "/admin_gestion_reclamos_pendientes";
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Reclamo no procesado.", ""));
                    return "/admin_procesar_reclamo_pendiente";
                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Reclamo no procesado.", ""));
                return "/admin_procesar_reclamo_pendiente";
            }

        }
    }

    //METODO PARA PROCESAR RECLAMOS ATENDIDOS
    public String actualizarReclamoAtendido() {
        if (reclamoSeleccionado.getFkCodTipoFinalizacionReclamo().getCodTipoFinalizacionReclamo().equals(0)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Debe seleccionar un Motivo de finalización.", ""));
            return "/admin_procesar_reclamo_atendido";
        } else if (reclamoSeleccionado.getDescripcionCulminacionReclamo().equals("") || reclamoSeleccionado.getDescripcionCulminacionReclamo().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Debe completar el campo de Descripción de tarea realizada.", ""));
            return "/admin_procesar_reclamo_atendido";
        } else {
            //Se completa el reclamo
            reclamoSeleccionado.setFkCodEstadoReclamo(new EstadosReclamos());
            reclamoSeleccionado.getFkCodEstadoReclamo().setCodEstadoReclamo(3);
            //   reclamoSeleccionado.getFkCodEstadoReclamo().setNombreEstadoReclamo("FINALIZADO");
            reclamoSeleccionado.setFkCodUsuarioCulminacion(new Usuarios());
            reclamoSeleccionado.setFkCodUsuarioCulminacion(recuperarUsuarioSession());
            reclamoSeleccionado.setFechaCulminacionReclamo(new Date());
            reclamoSeleccionado.setCantidadDiasProceso(cantidadDias(reclamoSeleccionado.getFechaAtencionReclamo()));

            String mensaje = reclamosSB.actualizarReclamos(reclamoSeleccionado);
            if (mensaje.equals("OK")) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Reclamo finalizado.", ""));
                //METODO PARA DESCARGAR PDF DESPUES DE ACTUALIZAR RECLAMO
                return "/admin_gestion_reclamos_atendidos";
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Reclamo no finalizado.", ""));
                return "/admin_procesar_reclamo_atendido";
            }
        }
    }

    //METODO PARA CALCULAR LA CANTIDAD DE DIAS ENTRE FECHA ACTUAL Y FECHA PASADA
    public int cantidadDias(Date fecha) {
        Calendar c = Calendar.getInstance();
        //Se crea un objeto calendario con la fecha del inicio del reclamo
        Calendar fechaInicio = new GregorianCalendar();
        fechaInicio.setTime(fecha);
        //Se crea un objeto calendario con la fecha actual
        Calendar hoy = Calendar.getInstance();
        //obtiene el dia
        c.setTimeInMillis(hoy.getTime().getTime() - fechaInicio.getTime().getTime());
        int dias = c.get(Calendar.DAY_OF_YEAR);
        return dias;
    }

    public String mostrarReclamosPendientes() {
        reclamosPendientes = reclamosSB.listarPorDependenciaEstado2(recuperarUsuarioSession().getFkCodPersona().getFkCodDependencia().getCodDependencia(), 1);
        return "/admin_gestion_reclamos_pendientes";
    }

    public String mostrarReclamosAtendidos() {
        reclamosAtendidos = reclamosSB.listarPorDependenciaEstado2(recuperarUsuarioSession().getFkCodPersona().getFkCodDependencia().getCodDependencia(), 2);
        return "/admin_gestion_reclamos_atendidos";
    }

    public String mostrarReclamosFinalizados() {
        reclamosFinalizados = reclamosSB.listarPorDependenciaEstado2(recuperarUsuarioSession().getFkCodPersona().getFkCodDependencia().getCodDependencia(), 3);
        return "/admin_gestion_reclamos_finalizados";
    }

    /**
     * @return the misReclamos
     */
    public List<Reclamos> getMisReclamos() {
        misReclamos = reclamosSB.listarPorUsuario(recuperarUsuarioSession().getLoginUsuario());
        return misReclamos;
    }

    /**
     * @param misReclamos the misReclamos to set
     */
    public void setMisReclamos(List<Reclamos> misReclamos) {
        this.misReclamos = misReclamos;
    }

    /**
     * @return the tiposDeReclamos
     */
    public List<TiposReclamos> getTiposDeReclamos() {
        tiposDeReclamos = tiposReclamosSB.listarTiposReclamos();
        return tiposDeReclamos;
    }

    /**
     * @param tiposDeReclamos the tiposDeReclamos to set
     */
    public void setTiposDeReclamos(List<TiposReclamos> tiposDeReclamos) {
        this.tiposDeReclamos = tiposDeReclamos;
    }

    /**
     * @return the nuevoReclamo
     */
    public Reclamos getNuevoReclamo() {
        return nuevoReclamo;
    }

    /**
     * @param nuevoReclamo the nuevoReclamo to set
     */
    public void setNuevoReclamo(Reclamos nuevoReclamo) {
        this.nuevoReclamo = nuevoReclamo;
    }

    /**
     * @return the emptyModel
     */
    public MapModel getEmptyModel() {
        return emptyModel;
    }

    /**
     * @param emptyModel the emptyModel to set
     */
    public void setEmptyModel(MapModel emptyModel) {
        this.emptyModel = emptyModel;
    }

    /**
     * @return the zoom
     */
    public int getZoom() {
        return zoom;
    }

    /**
     * @param zoom the zoom to set
     */
    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    /**
     * @return the tipoDeReclamosSeleccionado
     */
    public TiposReclamos getTipoDeReclamosSeleccionado() {
        return tipoDeReclamosSeleccionado;
    }

    /**
     * @param tipoDeReclamosSeleccionado the tipoDeReclamosSeleccionado to set
     */
    public void setTipoDeReclamosSeleccionado(TiposReclamos tipoDeReclamosSeleccionado) {
        this.tipoDeReclamosSeleccionado = tipoDeReclamosSeleccionado;
    }

    /**
     * @return the dirReclamo
     */
    public String getDirReclamo() {
        return dirReclamo;
    }

    /**
     * @param dirReclamo the dirReclamo to set
     */
    public void setDirReclamo(String dirReclamo) {
        this.dirReclamo = dirReclamo;
    }

    /**
     * @return the latituteLongitude
     */
    public LatLng getLatituteLongitude() {
        return latituteLongitude;
    }

    /**
     * @param latituteLongitude the latituteLongitude to set
     */
    public void setLatituteLongitude(LatLng latituteLongitude) {
        this.latituteLongitude = latituteLongitude;
    }

    /**
     * @return the imagenCargada
     */
    public DefaultStreamedContent getImagenCargada() {
        return imagenCargada;
    }

    /**
     * @param imagenCargada the imagenCargada to set
     */
    public void setImagenCargada(DefaultStreamedContent imagenCargada) {
        this.imagenCargada = imagenCargada;
    }

    /**
     * @return the imagenParaGuardar
     */
    public Imagenes getImagenParaGuardar() {
        return imagenParaGuardar;
    }

    /**
     * @param imagenParaGuardar the imagenParaGuardar to set
     */
    public void setImagenParaGuardar(Imagenes imagenParaGuardar) {
        this.imagenParaGuardar = imagenParaGuardar;
    }

    /**
     * @return the mostrarGraphicImage
     */
    public boolean isMostrarGraphicImage() {
        return mostrarGraphicImage;
    }

    /**
     * @param mostrarGraphicImage the mostrarGraphicImage to set
     */
    public void setMostrarGraphicImage(boolean mostrarGraphicImage) {
        this.mostrarGraphicImage = mostrarGraphicImage;
    }

    /**
     * @return the imagenSemaforo
     */
    public String getImagenSemaforo() {
        return imagenSemaforo;
    }

    /**
     * @param imagenSemaforo the imagenSemaforo to set
     */
    public void setImagenSemaforo(String imagenSemaforo) {
        this.imagenSemaforo = imagenSemaforo;
    }

    /**
     * @return the reclamosPendientes
     */
    public List<Reclamos> getReclamosPendientes() {
        return reclamosPendientes;
    }

    /**
     * @param reclamosPendientes the reclamosPendientes to set
     */
    public void setReclamosPendientes(List<Reclamos> reclamosPendientes) {
        this.reclamosPendientes = reclamosPendientes;
    }

    /**
     * @return the reclamosAtendidos
     */
    public List<Reclamos> getReclamosAtendidos() {
        return reclamosAtendidos;
    }

    /**
     * @param reclamosAtendidos the reclamosAtendidos to set
     */
    public void setReclamosAtendidos(List<Reclamos> reclamosAtendidos) {
        this.reclamosAtendidos = reclamosAtendidos;
    }

    /**
     * @return the reclamosFinalizados
     */
    public List<Reclamos> getReclamosFinalizados() {
        return reclamosFinalizados;
    }

    /**
     * @param reclamosFinalizados the reclamosFinalizados to set
     */
    public void setReclamosFinalizados(List<Reclamos> reclamosFinalizados) {
        this.reclamosFinalizados = reclamosFinalizados;
    }

    /**
     * @return the reclamoSeleccionado
     */
    public Reclamos getReclamoSeleccionado() {
        return reclamoSeleccionado;
    }

    /**
     * @param reclamoSeleccionado the reclamoSeleccionado to set
     */
    public void setReclamoSeleccionado(Reclamos reclamoSeleccionado) {
        this.reclamoSeleccionado = reclamoSeleccionado;
    }

    /**
     * @return the reclamosPorZona
     */
    public List<Reclamos> getReclamosPorZona() {
        return reclamosPorZona;
    }

    /**
     * @param reclamosPorZona the reclamosPorZona to set
     */
    public void setReclamosPorZona(List<Reclamos> reclamosPorZona) {
        this.reclamosPorZona = reclamosPorZona;
    }

    /**
     * @return the listTiposFinalizacionReclamos
     */
    public List<TiposFinalizacionReclamos> getListTiposFinalizacionReclamos() {
        listTiposFinalizacionReclamos = tiposFinalizacionReclamosSB.listarTiposFinalizacionReclamos();
        return listTiposFinalizacionReclamos;
    }

    /**
     * @param listTiposFinalizacionReclamos the listTiposFinalizacionReclamos to
     * set
     */
    public void setListTiposFinalizacionReclamos(List<TiposFinalizacionReclamos> listTiposFinalizacionReclamos) {
        this.listTiposFinalizacionReclamos = listTiposFinalizacionReclamos;
    }

    /**
     * @return the fechaInicio
     */
    public Date getFechaInicio() {
        return fechaInicio;
    }

    /**
     * @param fechaInicio the fechaInicio to set
     */
    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    /**
     * @return the fechaFin
     */
    public Date getFechaFin() {
        return fechaFin;
    }

    /**
     * @param fechaFin the fechaFin to set
     */
    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    /**
     * @return the nuevoUsuario
     */
    public Usuarios getNuevoUsuario() {
        return nuevoUsuario;
    }

    /**
     * @param nuevoUsuario the nuevoUsuario to set
     */
    public void setNuevoUsuario(Usuarios nuevoUsuario) {
        this.nuevoUsuario = nuevoUsuario;
    }

    /**
     * @return the activarCamposNuevoUsuario
     */
    public boolean isActivarCamposNuevoUsuario() {
        return activarCamposNuevoUsuario;
    }

    /**
     * @param activarCamposNuevoUsuario the activarCamposNuevoUsuario to set
     */
    public void setActivarCamposNuevoUsuario(boolean activarCamposNuevoUsuario) {
        this.activarCamposNuevoUsuario = activarCamposNuevoUsuario;
    }

    /**
     * @return the activarCampoDireccion
     */
    public boolean isActivarCampoDireccion() {
        return activarCampoDireccion;
    }

    /**
     * @param activarCampoDireccion the activarCampoDireccion to set
     */
    public void setActivarCampoDireccion(boolean activarCampoDireccion) {
        this.activarCampoDireccion = activarCampoDireccion;
    }

    /**
     * @return the activarCamposCuenta
     */
    public boolean isActivarCamposCuenta() {
        return activarCamposCuenta;
    }

    /**
     * @param activarCamposCuenta the activarCamposCuenta to set
     */
    public void setActivarCamposCuenta(boolean activarCamposCuenta) {
        this.activarCamposCuenta = activarCamposCuenta;
    }

    /**
     * @return the activarCamposTelefono
     */
    public boolean isActivarCamposTelefono() {
        return activarCamposTelefono;
    }

    /**
     * @param activarCamposTelefono the activarCamposTelefono to set
     */
    public void setActivarCamposTelefono(boolean activarCamposTelefono) {
        this.activarCamposTelefono = activarCamposTelefono;
    }

    /**
     * @return the mensajeCorreo
     */
    public String getMensajeCorreo() {
        return mensajeCorreo;
    }

    /**
     * @param mensajeCorreo the mensajeCorreo to set
     */
    public void setMensajeCorreo(String mensajeCorreo) {
        this.mensajeCorreo = mensajeCorreo;
    }

    /**
     * @return the marcaParaNuevoUsuario
     */
    public boolean isMarcaParaNuevoUsuario() {
        return marcaParaNuevoUsuario;
    }

    /**
     * @param marcaParaNuevoUsuario the marcaParaNuevoUsuario to set
     */
    public void setMarcaParaNuevoUsuario(boolean marcaParaNuevoUsuario) {
        this.marcaParaNuevoUsuario = marcaParaNuevoUsuario;
    }

    /**
     * @return the activarCampoCorreo
     */
    public boolean isActivarCampoCorreo() {
        return activarCampoCorreo;
    }

    /**
     * @param activarCampoCorreo the activarCampoCorreo to set
     */
    public void setActivarCampoCorreo(boolean activarCampoCorreo) {
        this.activarCampoCorreo = activarCampoCorreo;
    }

    /**
     * @return the reclamos
     */
    public List<Reclamos> getReclamos() {
//        reclamos = reclamosSB.listarReclamos();
        reclamos = reclamosSB.listarReclamos3();
        return reclamos;
    }

    /**
     * @param reclamos the reclamos to set
     */
    public void setReclamos(List<Reclamos> reclamos) {
        this.reclamos = reclamos;
    }

    /**
     * @return the codigoEstadoReclamo
     */
    public int getCodigoEstadoReclamo() {
        return codigoEstadoReclamo;
    }

    /**
     * @param codigoEstadoReclamo the codigoEstadoReclamo to set
     */
    public void setCodigoEstadoReclamo(int codigoEstadoReclamo) {
        this.codigoEstadoReclamo = codigoEstadoReclamo;
    }

    /**
     * @return the barrios
     */
    public List<Paises04Barrios> getBarrios() {
        barrios = barriosSB.listarBarrios();
        return barrios;
    }

    /**
     * @param barrios the barrios to set
     */
    public void setBarrios(List<Paises04Barrios> barrios) {
        this.barrios = barrios;
    }

    /**
     * @return the configuraciones
     */
    public Configuraciones getConfiguraciones() {
        return configuraciones;
    }

    /**
     * @param configuraciones the configuraciones to set
     */
    public void setConfiguraciones(Configuraciones configuraciones) {
        this.configuraciones = configuraciones;
    }

}
