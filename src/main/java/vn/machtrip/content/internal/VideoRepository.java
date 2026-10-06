package vn.machtrip.content.internal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.machtrip.content.PlaceVideo;

import java.util.List;
import java.util.UUID;

interface VideoRepository extends JpaRepository<VideoEntity, String> {

    @Query("""
            select new vn.machtrip.content.PlaceVideo(v.tiktokId, v.webVideoUrl, pv.tStartSec, pv.rank, v.createdAt,
                   v.playCount, v.likeCount, v.saveCount, v.commentCount, v.shareCount, v.language, v.embedStatus)
            from PlaceVideoEntity pv join VideoEntity v on v.tiktokId = pv.videoId
            where pv.placeId = :placeId
            order by pv.rank
            """)
    List<PlaceVideo> findByPlace(@Param("placeId") UUID placeId, Pageable pageable);
}
