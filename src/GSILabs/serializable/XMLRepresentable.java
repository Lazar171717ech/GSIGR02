/*
 * This document is part of the lab material for the subject:
 * Gestion de Sistemas de Informacion
 * to be released at the
 * Universidad Publica de Navarra
 * during the first semester of the Academic Year 2015-2016
 */
 
 package GSILabs.serializable;

import java.io.File;

/**
 * This interface embodies the ability of a a class of objects to represent itself
 * 	using XML. 
 * Note that it refers, in no manner, to its ability to create instances from such a representation.
 *
 * @author carlos.lopez
 * @version 1.0 (06/08/2013)    
 */
public interface XMLRepresentable {
    
    /**
     * Genera la representación en formato XML de la instancia actual.
     *
     * @return Cadena de texto con la representación XML del objeto.
     */
    public String toXML();
    
    /**
     * Almacena la representación XML de la instancia actual en el fichero indicado.
     *
     * @param f Fichero de destino donde se guardará el XML.
     * @return true si el almacenamiento se completó con éxito; false en caso contrario.
     */
    public boolean saveToXML(File f);
    
    /**
     * Almacena la representación XML de la instancia actual en la ruta especificada.
     *
     * @param filePath Ruta del fichero de destino.
     * @return true si el almacenamiento se completó con éxito; false en caso contrario.
     */
    public boolean saveToXML(String filePath);
    
}
