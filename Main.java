import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

class ConexionFallidaException extends Exception {
    public ConexionFallidaException(String mensaje) { super(mensaje); }
}

class LecturaAnomalaException extends Exception {
    public LecturaAnomalaException(String mensaje) { super(mensaje); }
}

interface ISensorRed {
    void conectar() throws ConexionFallidaException;
    double obtenerLectura() throws LecturaAnomalaException;
    void desconectar();
}

abstract class SensorBase implements ISensorRed {
    private static int totalSensoresCreados = 0; 
    
    protected String id; 
    private boolean activo; 

    public SensorBase(String id) {
        this.id = id;
        this.activo = false;
        totalSensoresCreados++;
    }

    public static int getTotalSensoresCreados() { return totalSensoresCreados; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public String getId() { return id; }
}

class SensorTemperatura extends SensorBase {
    private double umbralMaximo;

    public SensorTemperatura(String id, double umbralMaximo) {
        super(id);
        this.umbralMaximo = umbralMaximo;
    }

    @Override
    public void conectar() throws ConexionFallidaException {
        if (Math.random() < 0.1) { // 10% de probabilidad de fallo físico
            throw new ConexionFallidaException("Error de hardware en el sensor " + id);
        }
        setActivo(true);
        System.out.println("Sensor " + id + " conectado.");
    }

    @Override
    public double obtenerLectura() throws LecturaAnomalaException {
        if (!isActivo()) return 0.0;
        
        double lectura = 20.0 + (Math.random() * 15);
        if (lectura > umbralMaximo) {
            throw new LecturaAnomalaException("Temperatura atípica en " + id + ": " + String.format("%.2f", lectura) + "°C");
        }
        return lectura;
    }

    @Override
    public void desconectar() {
        setActivo(false);
        System.out.println("Sensor " + id + " desconectado.");
    }
}

class ManejadorArchivos {
    private static final String RUTA_ARCHIVO = "bitacora_sensores.txt";

    public static void guardarLog(String mensaje) {
        try (FileWriter fw = new FileWriter(RUTA_ARCHIVO, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(mensaje);
        } catch (IOException e) {
            System.out.println("Error: No se pudo escribir en la bitácora.");
        }
    }
}

public class Main {
    public static void main(String[] args) {
        List<ISensorRed> redSensores = new ArrayList<>();
        
        redSensores.add(new SensorTemperatura("TEMP-001", 30.0));
        redSensores.add(new SensorTemperatura("TEMP-002", 28.5));
        
        System.out.println("Sensores registrados: " + SensorBase.getTotalSensoresCreados());

        // Intento de conexión con manejo de excepciones
        for (ISensorRed sensor : redSensores) {
            try {
                sensor.conectar();
            } catch (ConexionFallidaException e) {
                System.out.println(e.getMessage());
                ManejadorArchivos.guardarLog("Fallo crítico: " + e.getMessage());
            }
        }

        // Ciclo de monitoreo y validación
        for (ISensorRed sensor : redSensores) {
            try {
                double dato = sensor.obtenerLectura();
                System.out.println("Lectura exitosa: " + String.format("%.2f", dato));
                ManejadorArchivos.guardarLog("Lectura normal: " + dato);
            } catch (LecturaAnomalaException e) {
                System.out.println("¡ALERTA! " + e.getMessage());
                ManejadorArchivos.guardarLog("ALERTA: " + e.getMessage());
            }
        }
    }
}