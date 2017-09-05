package uk.gov.hmcts.reform.draftstore.data;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.gov.hmcts.reform.draftstore.domain.SaveStatus;
import uk.gov.hmcts.reform.draftstore.exception.NoDraftFoundException;

import java.util.List;

import static uk.gov.hmcts.reform.draftstore.domain.SaveStatus.Created;
import static uk.gov.hmcts.reform.draftstore.domain.SaveStatus.Updated;

public class DraftStoreDAO {

    // region queries
    private static final String INSERT =
        "INSERT INTO draft_document (user_id, document_type, document) "
            + "VALUES (:userId, :type, cast(:document AS JSON))";

    private static final String UPDATE =
        "UPDATE draft_document "
            + "SET document = cast(:document AS JSON) "
            + "WHERE user_id = :userId "
            + "AND document_type = :type";

    private static final String DELETE =
        "DELETE FROM draft_document "
            + "WHERE user_id = :userId "
            + "AND document_type = :type";

    private static final String QUERY =
        "SELECT document FROM draft_document "
            + "WHERE user_id = :userId "
            + "AND document_type = :type";
    // endregion

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DraftStoreDAO(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SaveStatus insertOrUpdate(String userId, String type, String newDocument) {
        MapSqlParameterSource params =
            new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("type", type)
                .addValue("document", newDocument);

        int rows = jdbcTemplate.update(UPDATE, params);
        if (rows == 1) {
            return Updated;
        } else {
            jdbcTemplate.update(INSERT, params);
            return Created;
        }
    }

    public String retrieve(String userId, String type) {
        MapSqlParameterSource params =
            new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("type", type);

        List<String> documents = jdbcTemplate.query(QUERY, params, (rs, rowNum) -> rs.getString(1));

        return documents.stream().findFirst().orElseThrow(NoDraftFoundException::new);
    }

    public void delete(String userId, String type) {
        int rows =
            jdbcTemplate.update(
                DELETE,
                new MapSqlParameterSource()
                    .addValue("userId", userId)
                    .addValue("type", type)
            );
        if (rows == 0) {
            throw new NoDraftFoundException();
        }
    }
}

