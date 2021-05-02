package app.messages;

import java.sql.*;

import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

@Component
public class MessageRepository {
    private DataSource dataSource;
    final static Log logger = LogFactory.getLog(MessageRepository.class);

    public MessageRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Message saveMessage(Message message) {
        Connection c = DataSourceUtils.getConnection(dataSource);
        try {
            String insertSql = "INSERT INTO messages ( `id`, `text`, `created_date`) VALUE (null, ?, ?)";
            PreparedStatement ps = c.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, message.getText());
            ps.setTimestamp(2, new Timestamp(message.getCreatedDate().getTime()));
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet result = ps.getGeneratedKeys();
                if (result.next()) {
                    int id = result.getInt(1);
                    return new Message(id, message.getText(), message.getCreatedDate());
                } else {
                    logger.error("Failed to retrieve id. No row in result set");
                    return null;
                }
            } else {
                // insert 실패
                return null;
            }
        } catch (SQLException ex) {
            logger.error("Failed to save message", ex);
            try {
                c.close();
            } catch (SQLException e) {
                logger.error("Failed to close connection", e);
            }
        } finally {
            DataSourceUtils.releaseConnection(c, dataSource);
        }
        return null;
    }
}