package MultimediaServer.dto.multimedia;

import MultimediaServer.models.MultimediaEntity;

// multimediaPath: nombre del archivo relativo a app.storage.videos; para reproducir se usa GET /multimedia/{id}/video.
public record FullMultimediaDTO(
        Long id,
        Long movieSeriesId,
        String multimediaPath,
        String title,
        Integer season,
        Integer episodeNumber,
        Integer durationSeconds
) {
    public FullMultimediaDTO(MultimediaEntity e) {
        this(e.getId(), e.getMovieSeries().getId(), e.getMultimediaPath(), e.getTitle(),
                e.getSeason(), e.getEpisodeNumber(), e.getDurationSeconds());
    }
}
