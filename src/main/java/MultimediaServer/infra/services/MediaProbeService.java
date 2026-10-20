package MultimediaServer.infra.services;

import MultimediaServer.infra.errors.MyIntegrityValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

// Lee metadatos de vídeo con ffprobe (FFmpeg), que debe estar instalado en el servidor.
@Service
public class MediaProbeService {

    private static final long TIMEOUT_SECONDS = 60;

    @Value("${app.ffprobe.path}")
    private String ffprobePath;

    // Si ffprobe no reconoce el archivo, no es un vídeo válido (aunque el content type declarado lo fuera).
    public int getDurationSeconds(Path video) {
        ProcessBuilder builder = new ProcessBuilder(ffprobePath,
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                video.toString());
        builder.redirectError(ProcessBuilder.Redirect.DISCARD);

        Process process;
        try {
            process = builder.start();
        } catch (IOException e) {
            throw new MyIntegrityValidationException(
                    "No se pudo calcular la duración del vídeo: ffprobe no está disponible en el servidor",
                    500, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw invalidVideo();
            }
            if (process.exitValue() != 0) {
                throw invalidVideo();
            }
            double seconds = Double.parseDouble(output);
            if (seconds <= 0) {
                throw invalidVideo();
            }
            return (int) Math.max(1, Math.round(seconds));
        } catch (NumberFormatException | IOException e) {
            throw invalidVideo();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            throw invalidVideo();
        }
    }

    private MyIntegrityValidationException invalidVideo() {
        return new MyIntegrityValidationException("Error, el archivo no es un vídeo válido", 400, HttpStatus.BAD_REQUEST);
    }
}
