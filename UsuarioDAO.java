/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ModeloDAO;

import ModeloVO.RolVO;
import ModeloVO.UsuarioVO;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import util.ConexionBd;
import util.Crud;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import util.PropiedadesCorreo;



/**
 *
 * @author APRENDIZ
 */
public class UsuarioDAO extends ConexionBd implements Crud{
    //Declarar variables y/o objetos
    ConexionBd conec = new ConexionBd();
    private Connection conexion;
    private PreparedStatement puente;
    private ResultSet mensajero;
    private boolean operacion = false;
    private String sql;
    
    private String usuCedula="", usuNombre="", usuApellido="", usuCorreo="", usuDireccion="", usuTelefono="", usuPassword="", usuEstado="", usuRolId="";

    public UsuarioDAO() {
    }
    
    

    public UsuarioDAO(UsuarioVO usuVO){
        super();
        try {
            
            // Conectarse a la base de datos
            conexion = this.obtenerConexion();
            // Trae los datos del VO al DAO
            usuCedula = usuVO.getUsuCedula();
            usuNombre = usuVO.getUsuNombre();
            usuApellido = usuVO.getUsuApellido();
            usuTelefono = usuVO.getUsuTelefono();
            usuCorreo = usuVO.getUsuCorreo();
            usuDireccion = usuVO.getUsuDireccion();
            usuPassword = usuVO.getUsuPassword();
            usuEstado = usuVO.getUsuEstado();
            usuRolId = usuVO.getUsuRolId();
            
        } catch (Exception e) {
          Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE,null,e);
            
        }
        
    }

    public boolean restablecerContrasena(String cedulaUsuario) {
    try {
        // Verificar si la cédula existe en la base de datos
        sql = "SELECT UsuCorreo FROM tblUsuario WHERE UsuCedula = ?;";
        puente = conexion.prepareStatement(sql);
        puente.setString(1, cedulaUsuario);
        mensajero = puente.executeQuery();

        if (mensajero.next()) {
            // La cédula existe en la base de datos, proceder con el restablecimiento de contraseña
            String contrasenaGenerada = generarToken(10);
            sql = "UPDATE tblUsuario SET UsuPassword = ? WHERE UsuCedula = ?;";
            puente = conexion.prepareStatement(sql);
            puente.setString(1, contrasenaGenerada);
            puente.setString(2, cedulaUsuario);
            puente.executeUpdate();
            operacion = true;

            // Obtener el correo asociado a la cédula
            String correoUsuario = mensajero.getString("UsuCorreo");

            // Envío del correo con la nueva contraseña generada
            String mensaje = "Estimado/a Usuario,\n\n"
                + "Se ha restablecido su contraseña en nuestro sistema.\n"
                + "A continuación se muestra su nueva contraseña generada automáticamente:\n\n"
                + contrasenaGenerada + "\n\n"
                + "Le recomendamos cambiar su contraseña una vez que haya iniciado sesión.\n\n"
                + "Saludos,\n"
                + "Equipo de Administración";

            String asunto = "Restablecimiento de contraseña exitoso";
            String usuario = "techn0.check0ut@gmail.com"; // Cambiar por tu dirección de correo electrónico
            String destino = correoUsuario;
            String servidor = "smtp.gmail.com";
            String puerto = "587";
            String clave = "dhyostnzjleivjci";

            PropiedadesCorreo.envioCorreo(servidor, puerto, usuario, clave, destino, asunto, mensaje);
        } else {
            // La cédula no existe en la base de datos
            operacion = false;
        }
    } catch (Exception e) {
        Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE, null, e);
    } finally {
        try {
            this.cerrarConexion();
        } catch (Exception e) {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    return operacion;
}

    
    @Override
   public boolean agregarRegistro() {
    try {
        String contrasenaGenerada = generarToken(10); // Longitud del token deseada
        sql = "INSERT INTO tblusuario (UsuCedula, UsuNombre, UsuApellido, UsuTelefono, UsuCorreo, UsuDireccion, usuPassword, usuRolId) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        puente = conexion.prepareStatement(sql);
        puente.setString(1, usuCedula);
        puente.setString(2, usuNombre);
        puente.setString(3, usuApellido);
        puente.setString(4, usuTelefono);
        puente.setString(5, usuCorreo);
        puente.setString(6, usuDireccion);
        puente.setString(7, contrasenaGenerada);
        puente.setString(8, usuRolId);
        puente.executeUpdate();
        operacion = true;

        // Envío de correo electrónico
        String mensaje = "Estimado/a " + usuNombre+ ",\n\n"
                + "Se ha creado una cuenta para usted en nuestro sistema.\n"
                + "A continuación se muestra su contraseña generada automáticamente:\n\n"
                + contrasenaGenerada + "\n\n"
                + "Le recomendamos cambiar su contraseña una vez que haya iniciado sesión\n\n"
                + "Su usuario es el documento ingresado al momento de registrarse.\n\n"
                + "Saludos,\n"
                + "Equipo de Administración";

        String asunto = "Creación de cuenta exitosa";
        String usuario = "techn0.check0ut@gmail.com"; // Cambiar por tu dirección de correo electrónico
        String destino = usuCorreo;
        String servidor ="smtp.gmail.com";
        String puerto="587";
        String clave="dhyostnzjleivjci";

        PropiedadesCorreo.envioCorreo(servidor, puerto, usuario, clave, destino, asunto, mensaje);

    } catch (Exception e) {
        Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE, null, e);
    } finally {
        try {
            this.cerrarConexion();
        } catch (Exception e) {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    return operacion;
}

    private String generarToken(int longitud) {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder();

        for (int i = 0; i < longitud; i++) {
            int indice = random.nextInt(caracteres.length());
            token.append(caracteres.charAt(indice));
        }

        return token.toString();
    }  
   
    @Override
    public boolean actualizarRegistro() {
         try {
            sql="UPDATE tblUsuario SET UsuNombre = ?, UsuApellido = ?,UsuTelefono = ?,UsuCorreo = ?,UsuDireccion = ?,UsuPassword = ?,UsuRolId = ? WHERE UsuCedula = ?;";
            puente = conexion.prepareStatement(sql);
            puente.setString(1, usuNombre);
            puente.setString(2, usuApellido);
            puente.setString(3, usuTelefono);
            puente.setString(4, usuCorreo);
            puente.setString(5, usuDireccion);            
            puente.setString(6, usuPassword);
            puente.setString(7, usuRolId);
            puente.setString(8, usuCedula);
            puente.executeUpdate();
            operacion = true;
            
        } catch (Exception e) {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE,null,e);

        } finally{
            try {
                 this.cerrarConexion();
            } catch (Exception e) {
                Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE,null,e);

            }
           
        }
        return operacion;
    }
  public UsuarioVO consultarPorCedula(String cedula){
        UsuarioVO usuVO = null;
        
        try {
            conexion = this.obtenerConexion();
            sql= "select * from tblusuario where UsuCedula = ?";
            puente = conexion.prepareStatement(sql);
            puente.setString(1,cedula);
            mensajero=puente.executeQuery();
            while(mensajero.next())
            {
             usuVO = new UsuarioVO(cedula, mensajero.getString(2),mensajero.getString(3), mensajero.getString(5), 
                        mensajero.getString(6), mensajero.getString(4), mensajero.getString(7), mensajero.getString(8), mensajero.getString(9));
            }       

        } catch (Exception e) {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE,null,e);
        }finally{
            try {
                this.cerrarConexion();
            } catch (Exception e) {
                Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE,null,e);
            }
        }
        return usuVO;
    }
    
    public ArrayList<RolVO> listar(String cedula){
        
        ArrayList<RolVO> listaroles = new ArrayList<>();
            
        try {
            conexion = this.obtenerConexion();
            sql= "SELECT UsuRolId, UsuCedula FROM tblusuario where UsuCedula=?;";
            puente = conexion.prepareStatement(sql);
            puente.setString(1,cedula);
            mensajero = puente.executeQuery();
            while(mensajero.next())
            {
             RolVO rolVO= new RolVO(mensajero.getString(1), mensajero.getString(2));
            listaroles.add(rolVO);
           }
        } catch (Exception e) {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE,null,e);
        }finally{
            try {
                this.cerrarConexion();
            } catch (Exception e) {
                Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE,null,e);
            }
        }
        return listaroles;
    }
    public boolean eliminarRegistro() 
    {    
         try 
         {
             UsuarioVO usuVO= new UsuarioVO();
            conexion = this.obtenerConexion();
            sql = "UPDATE tblUsuario SET UsuEstado = 'Inactivo' WHERE UsuCedula = ?;";
            puente = conexion.prepareStatement(sql);
            puente.setString(1, usuCedula);
            puente.executeUpdate();
         }
        catch (Exception e) 
        {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE,null,e);
        } finally{
            try {
                 this.cerrarConexion();
            } catch (Exception e) {
                Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE,null,e);
            }
        }
        return operacion;
    }
    public boolean reactivarRegistro() 
    {    
         try 
         {
             UsuarioVO usuVO= new UsuarioVO();
            conexion = this.obtenerConexion();
            sql = "UPDATE tblUsuario SET UsuEstado = 'Activo' WHERE UsuCedula = ?;";
            puente = conexion.prepareStatement(sql);
            puente.setString(1, usuCedula);
            puente.executeUpdate();
         }
        catch (Exception e) 
        {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE,null,e);
        } finally{
            try {
                 this.cerrarConexion();
            } catch (Exception e) {
                Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE,null,e);
            }
        }
        return operacion;
    }
    public UsuarioVO consultarCedula(String Usuario)
    {
      UsuarioVO usuVO =null;
      try
      {
        conexion = this.obtenerConexion();
        sql = "select UsuCedula, UsuNombre, UsuPassword from outek.tblusuario where UsuCedula=?;";
        puente = conexion.prepareStatement(sql);
        puente.setString(1, Usuario);
        mensajero = puente.executeQuery();
        while(mensajero.next())
        {
            usuVO= new UsuarioVO(mensajero.getString(1), mensajero.getString(2),mensajero.getString(3), mensajero.getString(4), 
                        mensajero.getString(5), mensajero.getString(6), mensajero.getString(7),mensajero.getString(8), mensajero.getString(9));
        }
      }
       catch (Exception e) 
        {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE,null,e);
        } finally{
            try {
                 this.cerrarConexion();
            } catch (Exception e) {
                Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE,null,e);
            }
        }
        return usuVO;
    }
    
    public boolean actualizarRegistroCliente() {
         try {
            sql="UPDATE tblUsuario SET UsuNombre = ?, UsuApellido = ?,UsuTelefono = ?,UsuCorreo = ?,UsuDireccion = ? WHERE UsuCedula = ?;";
            puente = conexion.prepareStatement(sql);
            puente.setString(1, usuNombre);
            puente.setString(2, usuApellido);
            puente.setString(3, usuTelefono);
            puente.setString(4, usuCorreo);
            puente.setString(5, usuDireccion);
            puente.setString(6, usuCedula);
            puente.executeUpdate();
            operacion = true;
            
        } catch (Exception e) {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE,null,e);

        } finally{
            try {
                 this.cerrarConexion();
            } catch (Exception e) {
                Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE,null,e);

            }
           
        }
        return operacion;
    }
     public ArrayList<UsuarioVO> listar(){
        
        ArrayList<UsuarioVO> lista = new ArrayList<>();
            
        try {
            conexion = this.obtenerConexion();
            sql= "SELECT u.*, r.RolDenominacion FROM tblUsuario u INNER JOIN tblRol r ON u.UsuRolId = r.RolId;";
            puente = conexion.prepareStatement(sql);
            mensajero = puente.executeQuery();
            while(mensajero.next())
            {
             UsuarioVO usuVO= new UsuarioVO(mensajero.getString(1), mensajero.getString(2),mensajero.getString(3), mensajero.getString(4), 
                        mensajero.getString(5), mensajero.getString(6), mensajero.getString(7),mensajero.getString(8), mensajero.getString(9));
            lista.add(usuVO);
           }
        } catch (Exception e) {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE,null,e);
        }finally{
            try {
                this.cerrarConexion();
            } catch (Exception e) {
                Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE,null,e);
            }
        }
        return lista;
    }
     public UsuarioVO buscar(String cedula)
    {
      UsuarioVO c =new UsuarioVO();
      sql="select*from tblusuario where UsuCedula="+cedula;
      try
      {
        conexion = this.obtenerConexion();
        puente = conexion.prepareStatement(sql);
        mensajero = puente.executeQuery();
        while(mensajero.next())
        {
            c.setUsuCedula(mensajero.getString(1));
            c.setUsuNombre(mensajero.getString(2));
            c.setUsuDireccion(mensajero.getString(3));
            c.setUsuEstado(mensajero.getString(4));

        }
      }
      catch(Exception e)
      {
          
      }
      return c;
    }
}