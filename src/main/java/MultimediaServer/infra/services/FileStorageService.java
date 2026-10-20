package MultimediaServer.infra.services;

import MultimediaServer.infra.errors.MyIntegrityValidationException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

// Guarda archivos en disco sincronizado con la transacción: si hace rollback se borra el archivo nuevo,
// y un archivo reemplazado solo se borra cuando la transacción confirma.
@Service
public class FileStorageService {

    // allowedTypes: content type aceptado → extensión con la que se guarda. Devuelve el nombre del archivo (relativo a baseDir).
    // maxBytes: límite propio del tipo de archivo (spring.servlet.multipart.max-file-size es global y está pensado para vídeos).
    public String store(MultipartFile file, String baseDir, Map<String, String> allowedTypes, long maxBytes) {
        String extension = file.getContentType() != null ? allowedTypes.get(file.getContentType()) : null;
        if (file.isEmpty() || extension == null) {
            throw new MyIntegrityValidationException(
                    "Error, el archivo debe ser de tipo " + String.join(", ", allowedTypes.values()), 400, HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > maxBytes) {
            throw new MyIntegrityValidationException(
                    "Error, el archivo supera el tamaño máximo de " + maxBytes / (1024 * 1024) + "MB", 400, HttpStatus.BAD_REQUEST);
        }

        // Nombre generado: nunca se usa el nombre original (colisiones y rutas tipo "../").
        String fileName = UUID.randomUUID() + "." + extension;
        Path target = resolve(baseDir, fileName);
        try (InputStream in = file.getInputStream()) {
            Files.createDirectories(target.getParent());
            Files.copy(in, target);
        } catch (IOException e) {
            throw new MyIntegrityValidationException("Error al guardar el archivo", 500, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) {
                        deleteQuietly(target);
                    }
                }
            });
        }
        return fileName;
    }

    public Path resolve(String baseDir, String fileName) {
        return Path.of(baseDir).toAbsolutePath().resolve(fileName);
    }

    public Resource load(String baseDir, String fileName) {
        Path target = resolve(baseDir, fileName);
        if (!Files.isRegularFile(target)) {
            throw new MyIntegrityValidationException("El archivo indicado no existe", 404, HttpStatus.NOT_FOUND);
        }
        return new FileSystemResource(target);
    }

    public void deleteAfterCommit(String baseDir, String fileName) {
        if (fileName == null) {
            return;
        }
        Path target = resolve(baseDir, fileName);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteQuietly(target);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteQuietly(target);
            }
        });
    }

    // Un archivo que no se pudo borrar solo ocupa espacio: no debe hacer fallar una operación ya confirmada.
    private void deleteQuietly(Path target) {
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
        }
    }
}
