package com.fotolou.app.service.custom.storage;

import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Interface de contrat pour le service de stockage et de distribution de fichiers.
 */
public interface StorageService {
    /**
     * Enregistre un fichier téléversé et retourne son URL d'accès public (Cloudinary HTTPS ou local).
     *
     * @param file le fichier à enregistrer
     * @param folder le dossier de destination (ex: avatars, salons, produits)
     * @return l'URL publique d'accès
     * @throws IOException en cas d'erreur de lecture/écriture
     */
    String store(MultipartFile file, String folder) throws IOException;

    /**
     * Charge une ressource de fichier pour la distribution HTTP locale.
     *
     * @param filename le nom ou chemin relatif du fichier
     * @return la ressource de fichier
     */
    Resource loadAsResource(String filename);

    /**
     * Retourne le nom du provider de stockage actif (ex: 'cloudinary' ou 'local').
     */
    String getActiveProvider();
}
