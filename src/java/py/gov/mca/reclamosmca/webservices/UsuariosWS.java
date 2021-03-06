package py.gov.mca.reclamosmca.webservices;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.faces.bean.SessionScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;
import py.gov.mca.reclamosmca.entitys.EstadosUsuarios;
import py.gov.mca.reclamosmca.entitys.Personas;
import py.gov.mca.reclamosmca.entitys.Roles;
import py.gov.mca.reclamosmca.entitys.Usuarios;
import py.gov.mca.reclamosmca.sessionbeans.PersonasSB;
import py.gov.mca.reclamosmca.sessionbeans.UsuariosSB;
import py.gov.mca.reclamosmca.utiles.Converciones;

/**
 *
 * @author vinsfran
 */
@Stateless
@Path("/usuariosWeb")
@SessionScoped
public class UsuariosWS {

    @EJB
    private UsuariosSB usuariosSB;

    @EJB
    private PersonasSB personasSB;

    @POST
    @Path("/registrar_usuario_portal")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String registrarUsuarioPortal(String json) throws JSONException, ParseException {
        //System.out.println("JSON: " + json);
        SimpleDateFormat diaMesAnio = new SimpleDateFormat("dd-MM-yyyy");

        Usuarios usuario = new Usuarios();
        usuario.setFkCodPersona(new Personas());
        usuario.setFkCodEstadoUsuario(new EstadosUsuarios());
        usuario.setFkCodRol(new Roles());

        usuario.setLoginUsuario("");
        usuario.setClaveUsuario("");
        usuario.getFkCodPersona().setCedulaPersona("");
        usuario.getFkCodPersona().setNombrePersona("");
        usuario.getFkCodPersona().setApellidoPersona("");
        usuario.getFkCodPersona().setDireccionPersona("");
        usuario.getFkCodPersona().setCtaCtePersona("");
        usuario.getFkCodPersona().setTelefonoPersona("");

        JSONObject jsonObjectUsuario = new JSONObject(json);
        JSONObject jsonObjectPersona = new JSONObject(jsonObjectUsuario.getString("fkCodPersona"));

        usuario.setLoginUsuario(jsonObjectUsuario.getString("loginUsuario"));
        usuario.setClaveUsuario(jsonObjectUsuario.getString("claveUsuario"));
        usuario.getFkCodPersona().setCedulaPersona(jsonObjectPersona.getString("cedulaPersona"));
        usuario.getFkCodPersona().setNombrePersona(jsonObjectPersona.getString("nombrePersona"));
        usuario.getFkCodPersona().setApellidoPersona(jsonObjectPersona.getString("apellidoPersona"));
        usuario.getFkCodPersona().setFechaRegistroPersona(new Date());
        usuario.getFkCodPersona().setDireccionPersona(jsonObjectPersona.getString("direccionPersona"));
        usuario.getFkCodPersona().setCtaCtePersona(jsonObjectPersona.getString("ctaCtePersona"));
        usuario.getFkCodPersona().setTelefonoPersona(jsonObjectPersona.getString("telefonoPersona"));
        usuario.getFkCodPersona().setOrigenRegistro("appAndroid");
        usuario.getFkCodEstadoUsuario().setCodEstadoUsuario(2);
        usuario.getFkCodRol().setCodRol(6);

        Converciones c = new Converciones();
        //Se transforma a md5 la clave del usuario
        String contrasenaMD5 = c.getMD5(usuario.getClaveUsuario());
        if (contrasenaMD5 == null) {
            //Se controla si fue exitosa la conversion
            return "{\"status\":\"ERROR\", \"mensaje\":\"No se pudo crear. (MD5)\"}";
        } else {
            // Se reeplaza la clave enviada por la clave en md5
            usuario.setClaveUsuario(contrasenaMD5);

            int banderaRegistro = 0;
            //Consultar por cedula
            Personas personaExistente = personasSB.consultarPersonaCedula(usuario.getFkCodPersona().getCedulaPersona());
            //Consultar por login
            Usuarios usuarioExistente = usuariosSB.consultarUsuarios(usuario.getLoginUsuario());

            if (personaExistente == null) {
                if (usuarioExistente == null) {
                    banderaRegistro = 1;
                } else {
                    //Verificamos si existe el correo
                    return "{\"status\":\"ERROR\", \"mensaje\":\"Correo ya esta registrado.\"}";
                }
            } else if (personaExistente.getUsuariosList().isEmpty()) {
                if (usuarioExistente == null) {
                    banderaRegistro = 1;
                } else {
                    //Verificamos si existe el correo
                    return "{\"status\":\"ERROR\", \"mensaje\":\"Correo ya esta registrado.\"}";
                }
            } else {
                int banderaUsuarioWeb = 0;
                for (int i = 0; personaExistente.getUsuariosList().size() > i; i++) {
                    if (personaExistente.getUsuariosList().get(i).getFkCodRol().getCodRol().equals(6)) {
                        banderaUsuarioWeb = 1;
                    }
                }
                if (banderaUsuarioWeb == 1) {
                    //Verificamos si existe el correo
                    return "{\"status\":\"ERROR\", \"mensaje\":\"Correo ya esta registrado.\"}";
                } else {
                    banderaRegistro = 1;
                }
            }

            if (banderaRegistro == 0) {
                return "{\"status\":\"ERROR\", \"mensaje\":\"Cedula ya existe.\"}";
            } else {
                String resultado = usuariosSB.crearUsuariosWeb(usuario);
                if (resultado.equals("OK")) {
                    return "{\"status\":\"OK\", \"mensaje\":\"Cuenta registrada.\"}";
                } else {
                    return "{\"status\":\"ERROR\", \"mensaje\":\"" + resultado + "\"}";
                }
            }
        }
    }

    @POST
    @Path("/registrarUsuariosWeb")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String registrarUsuariosWeb(String json) throws JSONException, ParseException {
        //System.out.println("JSON: " + json);
        SimpleDateFormat diaMesAnio = new SimpleDateFormat("dd-MM-yyyy");

        Usuarios usuario = new Usuarios();
        usuario.setFkCodPersona(new Personas());
        usuario.setFkCodEstadoUsuario(new EstadosUsuarios());
        usuario.setFkCodRol(new Roles());

        usuario.setLoginUsuario("");
        usuario.setClaveUsuario("");
        usuario.getFkCodPersona().setCedulaPersona("");
        usuario.getFkCodPersona().setNombrePersona("");
        usuario.getFkCodPersona().setApellidoPersona("");
        usuario.getFkCodPersona().setDireccionPersona("");
        usuario.getFkCodPersona().setCtaCtePersona("");
        usuario.getFkCodPersona().setTelefonoPersona("");

        JSONObject jsonObjectUsuario = new JSONObject(json);
        JSONObject jsonObjectPersona = new JSONObject(jsonObjectUsuario.getString("fkCodPersona"));

        usuario.setLoginUsuario(jsonObjectUsuario.getString("loginUsuario"));
        usuario.setClaveUsuario(jsonObjectUsuario.getString("claveUsuario"));
        usuario.getFkCodPersona().setCedulaPersona(jsonObjectPersona.getString("cedulaPersona"));
        usuario.getFkCodPersona().setNombrePersona(jsonObjectPersona.getString("nombrePersona"));
        usuario.getFkCodPersona().setApellidoPersona(jsonObjectPersona.getString("apellidoPersona"));
        usuario.getFkCodPersona().setFechaRegistroPersona(new Date());
        usuario.getFkCodPersona().setDireccionPersona(jsonObjectPersona.getString("direccionPersona"));
        usuario.getFkCodPersona().setCtaCtePersona(jsonObjectPersona.getString("ctaCtePersona"));
        usuario.getFkCodPersona().setTelefonoPersona(jsonObjectPersona.getString("telefonoPersona"));
        usuario.getFkCodPersona().setOrigenRegistro("appAndroid");
        usuario.getFkCodEstadoUsuario().setCodEstadoUsuario(2);
        usuario.getFkCodRol().setCodRol(6);

        Converciones c = new Converciones();
        //Se transforma a md5 la clave del usuario
        String contrasenaMD5 = c.getMD5(usuario.getClaveUsuario());
        if (contrasenaMD5 == null) {
            //Se controla si fue exitosa la conversion
            return "{\"status\":\"ERROR\", \"mensaje\":\"No se pudo crear. (MD5)\"}";
        } else {
            // Se reeplaza la clave enviada por la clave en md5
            usuario.setClaveUsuario(contrasenaMD5);

            int banderaRegistro = 0;
            //Consultar por cedula
            Personas personaExistente = personasSB.consultarPersonaCedula(usuario.getFkCodPersona().getCedulaPersona());
            //Consultar por login
            Usuarios usuarioExistente = usuariosSB.consultarUsuarios(usuario.getLoginUsuario());

            if (personaExistente == null) {
                if (usuarioExistente == null) {
                    banderaRegistro = 1;
                } else {
                    //Verificamos si existe el correo
                    return "{\"status\":\"ERROR\", \"mensaje\":\"Correo ya esta registrado.\"}";
                }
            } else if (personaExistente.getUsuariosList().isEmpty()) {
                if (usuarioExistente == null) {
                    banderaRegistro = 1;
                } else {
                    //Verificamos si existe el correo
                    return "{\"status\":\"ERROR\", \"mensaje\":\"Correo ya esta registrado.\"}";
                }
            } else {
                int banderaUsuarioWeb = 0;
                for (int i = 0; personaExistente.getUsuariosList().size() > i; i++) {
                    if (personaExistente.getUsuariosList().get(i).getFkCodRol().getCodRol().equals(6)) {
                        banderaUsuarioWeb = 1;
                    }
                }
                if (banderaUsuarioWeb == 1) {
                    //Verificamos si existe el correo
                    return "{\"status\":\"ERROR\", \"mensaje\":\"Correo ya esta registrado.\"}";
                } else {
                    banderaRegistro = 1;
                }
            }

            if (banderaRegistro == 0) {
                return "{\"status\":\"ERROR\", \"mensaje\":\"Cedula ya existe.\"}";
            } else {
                String resultado = usuariosSB.crearUsuariosWeb(usuario);
                if (resultado.equals("OK")) {
                    return "{\"status\":\"OK\", \"mensaje\":\"Cuenta registrada.\"}";
                } else {
                    return "{\"status\":\"ERROR\", \"mensaje\":\"" + resultado + "\"}";
                }
            }
        }
    }

    //Metodo para realizar el ingreso desde WS retorna un status y mensaje
    @POST
    @Path("/ingresarUsuariosWeb")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String ingresarUsuariosWeb(String json) throws JSONException, ParseException {
        //Este metodo recibe en formato json los parametros loginUsuario y claveUsuario
        JSONObject jsonObject = new JSONObject(json);
        String loginUsuario = jsonObject.getString("loginUsuario");
        String claveUsuario = jsonObject.getString("claveUsuario");
        Usuarios usuario = usuariosSB.consultarUsuarios(loginUsuario);

        Converciones c = new Converciones();
        String contrasenaMD5 = c.getMD5(claveUsuario);

        if (usuario != null) {
            if (usuario.getClaveUsuario().substring(0, 6).equals(jsonObject.get("claveUsuario").toString()) && usuario.getFkCodEstadoUsuario().getCodEstadoUsuario().equals(2)) {
                EstadosUsuarios estado = new EstadosUsuarios();
                estado.setCodEstadoUsuario(1);
                usuario.setFkCodEstadoUsuario(estado);
                String resultado = usuariosSB.actualizarUsuariosWeb(usuario);
                if (resultado.equals("OK")) {
                    return "{\"status\":\"OK_1\", \"mensaje\":\"Su cuenta esta activa.\"}";
                } else {
                    return "{\"status\":\"ERROR\", \"mensaje\":\"No se pudo activar su cuenta, intentelo de nuevo.\n" + resultado + "\"}";
                }
            } else if (usuario.getClaveUsuario().substring(0, 6).equals(claveUsuario) && usuario.getFkCodEstadoUsuario().getCodEstadoUsuario().equals(3)) {
                return "{\"status\":\"OK_2\", \"mensaje\":\"Contraseña reestablecida. \nPor favor cambie su contraseña.\"}";
            } else if (contrasenaMD5 == null) {
                return "{\"status\":\"ERROR\", \"mensaje\":\"No se pudo crear. (MD5)\"}";
            } else if (usuario.getClaveUsuario().equals(contrasenaMD5) && usuario.getFkCodEstadoUsuario().getCodEstadoUsuario().equals(1)) {
                return "{\"status\":\"OK\", \"mensaje\":\"Bienvenido! \n" + usuario.getFkCodPersona().getNombrePersona() + "\"}";
            } else {
                return "{\"status\":\"ERROR\", \"mensaje\":\"Contraseña no valida.\"}";
            }
        } else {
            return "{\"status\":\"ERROR\", \"mensaje\":\"No existe usuario.\"}";
        }
    }

    @POST
    @Path("/ingresoUsuario")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String ingresoUsuario(String json) throws JSONException, ParseException {
        JSONObject jsonObject = new JSONObject(json);
        String loginUsuario = jsonObject.getString("loginUsuario");
        String claveUsuario = jsonObject.getString("claveUsuario");

        String retorno = "{\"status\":\"ERROR\", \"dato\":\"No existe usuario.\"}";

        Usuarios usuario = usuariosSB.consultarUsuarios(loginUsuario);

        Converciones c = new Converciones();
        String contrasenaMD5 = c.getMD5(claveUsuario);
        Personas personaAux;
        if (usuario != null) {
            if (contrasenaMD5 != null) {
                if (usuario.getClaveUsuario().equals(contrasenaMD5)) {

                    personaAux = usuario.getFkCodPersona();

                    retorno = "{\"status\":\"OK\", \"dato\":{\"cedula\":\"" + personaAux.getCedulaPersona() + "\","
                            + "\"nombres\":\"" + personaAux.getNombrePersona() + "\","
                            + "\"apellidos\":\"" + personaAux.getApellidoPersona() + "\","
                            + "\"direccion\":\"" + personaAux.getDireccionPersona() + "\","
                            + "\"cuentaCorriente\":\"" + personaAux.getCtaCtePersona() + "\","
                            + "\"telefono\":\"" + personaAux.getTelefonoPersona() + "\"}}";
                }
            }
        }
        return retorno;

    }

    //Metodo para realizar el reseteo de la contraseña
    @POST
    @Path("/recuperarContrasenaUsuariosWeb")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String recuperarContrasenaUsuariosWeb(String json) throws JSONException, ParseException {
        //Este metodo recibe en formato json el parametro loginUsuario
        JSONObject jsonObject = new JSONObject(json);
        String loginUsuario = jsonObject.getString("loginUsuario");
        Usuarios usuario = usuariosSB.consultarUsuarios(loginUsuario);

        if (usuario != null) {
            //Cambia estado del usuario a RESETCLAVE
            EstadosUsuarios estado = new EstadosUsuarios();
            estado.setCodEstadoUsuario(3);
            usuario.setFkCodEstadoUsuario(estado);
            String resultado = usuariosSB.actualizarUsuariosWeb(usuario);
            if (resultado.equals("OK")) {
                return "{\"status\":\"OK\", \"mensaje\":\"Contraseña reestablecida.\"}";
            } else {
                return "{\"status\":\"ERROR\", \"mensaje\":\"No se pudo reestablecer su contraseña, intentelo de nuevo.\n" + resultado + "\"}";
            }
        } else {
            return "{\"status\":\"ERROR\", \"mensaje\":\"No existe usuario.\"}";
        }
    }

    //Metodo para realizar el cambio de contraseña cuando se solicita reseteo de clave
    @POST
    @Path("/cambiarContrasenaResetUsuariosWeb")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String cambiarContrasenaResetUsuariosWeb(String json) throws JSONException, ParseException {
        //Este metodo recibe en formato json los parametros loginUsuario y claveUsuario
        JSONObject jsonObject = new JSONObject(json);
        String loginUsuario = jsonObject.getString("loginUsuario");
        String claveUsuario = jsonObject.getString("claveUsuario");
        Usuarios usuario = usuariosSB.consultarUsuarios(loginUsuario);

        Converciones c = new Converciones();
        String contrasenaMD5 = c.getMD5(claveUsuario);

        if (usuario != null) {
            if (contrasenaMD5 == null) {
                return "{\"status\":\"ERROR\", \"mensaje\":\"No se pudo crear. (MD5)\"}";
            } else {
                EstadosUsuarios estado = new EstadosUsuarios();
                estado.setCodEstadoUsuario(1);
                usuario.setFkCodEstadoUsuario(estado);
                usuario.setClaveUsuario(contrasenaMD5);
                String resultado = usuariosSB.actualizarUsuariosWeb(usuario);
                if (resultado.equals("OK")) {
                    return "{\"status\":\"OK\", \"mensaje\":\"Contraseña cambiada.\"}";
                } else {
                    return "{\"status\":\"ERROR\", \"mensaje\":\"No se pudo cambiar su contraseña, intentelo de nuevo.\n" + resultado + "\"}";
                }
            }
        } else {
            return "{\"status\":\"ERROR\", \"mensaje\":\"No existe usuario.\"}";
        }
    }

    //Metodo para realizar el cambio de contraseña cuando esta logueado
    @POST
    @Path("/cambiarContrasenaUsuariosWeb")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String cambiarContrasenaUsuariosWeb(String json) throws JSONException, ParseException {
        //Este metodo recibe en formato json los parametros loginUsuario y claveUsuario
        JSONObject jsonObject = new JSONObject(json);
        String loginUsuario = jsonObject.getString("loginUsuario");
        String claveUsuario = jsonObject.getString("claveUsuario");
        String claveUsuarioNueva = jsonObject.getString("claveUsuarioNueva");
        Usuarios usuario = usuariosSB.consultarUsuarios(loginUsuario);

        Converciones c = new Converciones();
        String contrasenaMD5 = c.getMD5(claveUsuario);
        String contrasenaNuevaMD5 = c.getMD5(claveUsuarioNueva);
        if (usuario != null) {
            if (contrasenaMD5 == null) {
                return "{\"status\":\"ERROR\", \"mensaje\":\"No se pudo crear. (MD5)\"}";
            } else if (contrasenaNuevaMD5 == null) {
                return "{\"status\":\"ERROR\", \"mensaje\":\"No se pudo crear. (MD5) Nueva\"}";
            } else if (usuario.getClaveUsuario().equals(contrasenaMD5)) {
                usuario.setClaveUsuario(contrasenaMD5);
                String resultado = usuariosSB.actualizarUsuariosWeb(usuario);
                if (resultado.equals("OK")) {
                    return "{\"status\":\"OK\", \"mensaje\":\"Contraseña cambiada.\"}";
                } else {
                    return "{\"status\":\"ERROR\", \"mensaje\":\"No se pudo cambiar su contraseña, intentelo de nuevo.\n" + resultado + "\"}";
                }
            } else {
                return "{\"status\":\"ERROR\", \"mensaje\":\"Contraseña actual no es valida.\"}";
            }
        } else {
            return "{\"status\":\"ERROR\", \"mensaje\":\"No existe usuario.\"}";
        }
    }

    @POST
    @Path("/consultarUsuarioWebPorLoginUsuario")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Usuarios consultarUsuarioWebPorLoginUsuario(String json) throws JSONException, ParseException {
        JSONObject jsonObject = new JSONObject(json);
        Usuarios usuario = usuariosSB.consultarUsuarios(jsonObject.get("loginUsuario").toString());

        if (usuario.getFkCodPersona().getDireccionPersona().equals("") || usuario.getFkCodPersona().getDireccionPersona() == null) {
            usuario.getFkCodPersona().setDireccionPersona(" ");
        }
        if (usuario.getFkCodPersona().getCtaCtePersona().equals("") || usuario.getFkCodPersona().getCtaCtePersona() == null) {
            usuario.getFkCodPersona().setCtaCtePersona(" ");
        }
        if (usuario.getFkCodPersona().getTelefonoPersona().equals("") || usuario.getFkCodPersona().getTelefonoPersona() == null) {
            usuario.getFkCodPersona().setTelefonoPersona(" ");
        }
//        SimpleDateFormat aaaammdd = new SimpleDateFormat("yyyy-MM-dd");
//        String amd = aaaammdd.format(usuariosWeb.getFechaRegistroWeb());
//        System.out.println("FECHA: " + amd);
//        usuariosWeb.setFechaRegistroWeb(aaaammdd.parse(amd));
        return usuario;
    }

    @POST
    @Path("/actualizarUsuariosWeb")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String actualizarUsuariosWeb(String json) throws JSONException, ParseException {
        SimpleDateFormat diaMesAnio = new SimpleDateFormat("dd-MM-yyyy");
        JSONObject jsonObjectUsuario = new JSONObject(json);
        Usuarios usuario = usuariosSB.consultarUsuariosPorCodigo(jsonObjectUsuario.getInt("codUsuario"));
        usuario.setClaveUsuario(jsonObjectUsuario.getString("claveUsuario"));
        JSONObject jsonObjectPersona = new JSONObject(jsonObjectUsuario.getString("fkCodPersona"));
        //   usuario.setFkCodPersona(new Personas());
        usuario.getFkCodPersona().setCedulaPersona(jsonObjectPersona.getString("cedulaPersona"));
        usuario.getFkCodPersona().setNombrePersona(jsonObjectPersona.getString("nombrePersona"));
        usuario.getFkCodPersona().setApellidoPersona(jsonObjectPersona.getString("apellidoPersona"));
        usuario.getFkCodPersona().setDireccionPersona(jsonObjectPersona.getString("direccionPersona"));
        usuario.getFkCodPersona().setCtaCtePersona(jsonObjectPersona.getString("ctaCtePersona"));
        usuario.getFkCodPersona().setTelefonoPersona(jsonObjectPersona.getString("telefonoPersona"));
        // JSONObject jsonObjectEstaso = new JSONObject(jsonObjectUsuario.getString("fkCodEstadoUsuario"));
        //  usuario.getFkCodEstadoUsuario().setCodEstadoUsuario(jsonObjectEstaso.getInt("codEstadoUsuario"));

        String respuesta = usuariosSB.actualizarUsuariosWeb(usuario);
        if (respuesta.equals("OK")) {
            return "{\"status\":\"OK\", \"mensaje\":\"Datos Actualizados.\"}";
        } else {
            return "{\"status\":\"ERROR\", \"mensaje\":\"" + respuesta + "\"}";
        }

    }

    @POST
    @Path("/consultarUsuariosWebPorCedula")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String consultarUsuariosWebPorCedula(String json) throws JSONException, ParseException {
        JSONObject jsonObject = new JSONObject(json);
        return usuariosSB.consultarUsuariosPorCedula(jsonObject.get("cedulaPersona").toString());
    }

    @GET
    @Path("/listarUsuariosWeb")
    @Produces({MediaType.APPLICATION_JSON})
    public List<Usuarios> listarUsuariosWeb() {
        return usuariosSB.listarUsuarios();
    }
}
