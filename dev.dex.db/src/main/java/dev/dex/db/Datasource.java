package dev.dex.db;

import dev.dex.common.*;

import java.sql.*;
import java.util.*;

import static java.lang.Thread.*;

public class Datasource {
    private static Datasource instance = new Datasource();

    public static final String DB_NAME = "music.db";
    public static final String CONNECTION_STRING = "jdbc:sqlite:E:\\IntelliJProjects\\JavaMasterclass\\Section25" +
            "\\MusicUI\\" + DB_NAME;
    public static final String TABLE_ALBUMS = "albums";
    public static final String COLUMN_ALBUMS_ID = "_id";
    public static final String COLUMN_ALBUMS_NAME = "name";
    public static final String COLUMN_ALBUMS_ARTIST = "artist";
    public static final int INDEX_ALBUMS_ID = 1;
    public static final int INDEX_ALBUMS_NAME = 2;
    public static final int INDEX_ALBUMS_ARTIST = 3;

    public static final String TABLE_ARTISTS = "artists";
    public static final String COLUMN_ARTISTS_ID = "_id";
    public static final String COLUMN_ARTISTS_NAME = "name";
    public static final int INDEX_ARTISTS_ID = 1;
    public static final int INDEX_ARTISTS_NAME = 2;

    public static final String TABLE_SONGS = "songs";
    public static final String COLUMN_SONGS_ID = "_id";
    public static final String COLUMN_SONGS_TRACK = "track";
    public static final String COLUMN_SONGS_TITLE = "title";
    public static final String COLUMN_SONGS_ALBUM = "album";
    public static final int INDEX_SONGS_ID = 1;
    public static final int INDEX_SONGS_TRACK = 2;
    public static final int INDEX_SONGS_TITLE = 3;
    public static final int INDEX_SONGS_ALBUM = 4;

    public static final int ORDER_BY_NONE = 1;
    public static final int ORDER_BY_ASC = 2;
    public static final int ORDER_BY_DESC = 3;

    public static final String QUERY_ARTISTS_START = String.format("SELECT * FROM %s", TABLE_ARTISTS);
    public static final String QUERY_ARTISTS_ORDER = String.format(" ORDER BY %s COLLATE NOCASE", COLUMN_ARTISTS_NAME);

    public static final String QUERY_ALBUMS_FROM_ARTIST_START = String.format("SELECT %s.%s FROM %s INNER JOIN %s ON %s.%s=%s.%s " +
                    "WHERE %s.%s = ", TABLE_ALBUMS, COLUMN_ALBUMS_NAME, TABLE_ALBUMS, TABLE_ARTISTS, TABLE_ALBUMS, COLUMN_ALBUMS_ARTIST,
            TABLE_ARTISTS, COLUMN_ARTISTS_ID, TABLE_ARTISTS, COLUMN_ARTISTS_NAME);
    public static final String QUERY_ALBUMS_FROM_ARTIST_ORDER = String.format(" ORDER BY %s.%s COLLATE NOCASE", TABLE_ALBUMS,
            COLUMN_ALBUMS_NAME);

    public static final String QUERY_ARTIST_OF_SONG_START = String.format("SELECT %s.%s, %s.%s, %s.%s FROM %s " +
                    "INNER JOIN %s ON %s.%s=%s.%s INNER JOIN %s ON %s.%s=%s.%s WHERE %s.%s=", TABLE_ARTISTS, COLUMN_ARTISTS_NAME,
            TABLE_ALBUMS, COLUMN_ALBUMS_NAME, TABLE_SONGS, COLUMN_SONGS_TRACK, TABLE_SONGS, TABLE_ALBUMS, TABLE_SONGS,
            COLUMN_SONGS_ALBUM, TABLE_ALBUMS, COLUMN_ALBUMS_ID, TABLE_ARTISTS, TABLE_ALBUMS, COLUMN_ALBUMS_ARTIST, TABLE_ARTISTS,
            COLUMN_ARTISTS_ID, TABLE_SONGS, COLUMN_SONGS_TITLE);
    public static final String QUERY_ARTIST_OF_SONG_ORDER = String.format(" ORDER BY %s.%s, %s.%s COLLATE NOCASE", TABLE_ARTISTS,
            COLUMN_ARTISTS_NAME, TABLE_ALBUMS, COLUMN_ARTISTS_NAME);

    public static final String VIEW_ARTIST_LIST = "artist_list";
    public static final String VIEW_ARTIST_LIST_START = "CREATE VIEW IF NOT EXISTS " + VIEW_ARTIST_LIST +
            "(artist, album, track, title) AS SELECT " + TABLE_ARTISTS + "." + COLUMN_ARTISTS_NAME + ", " + TABLE_ALBUMS + "." +
            COLUMN_ALBUMS_NAME + ", " + TABLE_SONGS + "." + COLUMN_SONGS_TRACK + ", " + TABLE_SONGS + "." + COLUMN_SONGS_TITLE +
            " FROM " + TABLE_SONGS + " INNER JOIN " + TABLE_ALBUMS + " ON " + TABLE_SONGS + "." + COLUMN_SONGS_ALBUM + "=" +
            TABLE_ALBUMS + "." + COLUMN_ALBUMS_ID + " INNER JOIN " + TABLE_ARTISTS + " ON " + TABLE_ALBUMS + "." +
            COLUMN_ALBUMS_ARTIST + "=" + TABLE_ARTISTS + "." + COLUMN_ARTISTS_ID + " ORDER BY " + TABLE_ARTISTS + "." +
            COLUMN_ARTISTS_NAME + ", " + TABLE_ALBUMS + "." + COLUMN_ALBUMS_NAME + ", " + TABLE_SONGS + "." + COLUMN_SONGS_TRACK;

    public static final String QUERY_VIEW_SONG_ARTIST = "SELECT artist, album, track FROM " + VIEW_ARTIST_LIST +
            " WHERE title=\"";

    public static final String QUERY_VIEW_SONG_INFO_PREP = "SELECT " + COLUMN_ALBUMS_ARTIST + ", " + COLUMN_SONGS_ALBUM + ", " +
            COLUMN_SONGS_TRACK + " FROM " + VIEW_ARTIST_LIST + " WHERE " + COLUMN_SONGS_TITLE + " = ?";

    public static final String INSERT_ARTIST = "INSERT INTO " + TABLE_ARTISTS + "(" + COLUMN_ARTISTS_NAME + ") VALUES(?)";
    public static final String INSERT_ALBUM = "INSERT INTO " + TABLE_ALBUMS + "(" + COLUMN_ALBUMS_NAME + ", " + COLUMN_ALBUMS_ARTIST +
        ") VALUES(?,?)";
    public static final String INSERT_SONG = "INSERT INTO " + TABLE_SONGS + "(" + COLUMN_SONGS_TRACK + ", " + COLUMN_SONGS_TITLE +
            ", " + COLUMN_SONGS_ALBUM + ") VALUES(?,?,?)";

    public static final String QUERY_ARTIST = "SELECT " + COLUMN_ARTISTS_ID + " FROM " + TABLE_ARTISTS + " WHERE " +
            COLUMN_ARTISTS_NAME + "=?";
    public static final String QUERY_ALBUM = "SELECT " + COLUMN_ALBUMS_ID + " FROM " + TABLE_ALBUMS + " WHERE " +
            COLUMN_ALBUMS_NAME + "=?";

    public static final String QUERY_ALBUMS_BY_ARTIST_ID = "SELECT * FROM " + TABLE_ALBUMS + " WHERE " + COLUMN_ALBUMS_ARTIST +
            "=? ORDER BY " + COLUMN_ALBUMS_NAME + " COLLATE NOCASE";

    public static final String UPDATE_ARTIST = "UPDATE " + TABLE_ARTISTS + " SET " + COLUMN_ARTISTS_NAME + "=? WHERE " +
            COLUMN_ARTISTS_ID + "=?";


    private Connection conn;
    private PreparedStatement querySongInfoView;
    private PreparedStatement insertArtist, insertAlbum, insertSong;
    private PreparedStatement queryArtist, queryAlbum;
    private PreparedStatement queryAlbumsByArtistId;
    private PreparedStatement updateArtistStmt;

    private Datasource() {

    }

    public static Datasource getInstance() {
        return instance;
    }

    public boolean open() {
        try {
            conn = DriverManager.getConnection(CONNECTION_STRING);
            querySongInfoView = conn.prepareStatement(QUERY_VIEW_SONG_INFO_PREP);
            insertArtist = conn.prepareStatement(INSERT_ARTIST, Statement.RETURN_GENERATED_KEYS);
            insertAlbum = conn.prepareStatement(INSERT_ALBUM, Statement.RETURN_GENERATED_KEYS);
            insertSong = conn.prepareStatement(INSERT_SONG);
            queryArtist = conn.prepareStatement(QUERY_ARTIST);
            queryAlbum = conn.prepareStatement(QUERY_ALBUM);
            queryAlbumsByArtistId = conn.prepareStatement(QUERY_ALBUMS_BY_ARTIST_ID);
            updateArtistStmt = conn.prepareStatement(UPDATE_ARTIST);
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void close() {
        try {
            if (querySongInfoView != null) querySongInfoView.close();
            if (insertArtist != null) insertArtist.close();
            if (insertAlbum != null) insertAlbum.close();
            if (insertSong != null) insertSong.close();
            if (queryArtist != null) queryArtist.close();
            if (queryAlbum != null) queryAlbum.close();
            if (queryAlbumsByArtistId != null) queryAlbumsByArtistId.close();
            if (updateArtistStmt != null) updateArtistStmt.close();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        try {
            if (conn != null)
                conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public List<Artist> queryArtists(int order) {
        StringBuilder sb = new StringBuilder(QUERY_ARTISTS_START);
        if (order != ORDER_BY_NONE) {
            sb.append(QUERY_ARTISTS_ORDER + (order == ORDER_BY_DESC ? " desc" : " asc"));
        }
        try (Statement statement = conn.createStatement();
             ResultSet res = statement.executeQuery(sb.toString())) {
            List<Artist> artists = new ArrayList<>();
            while (res.next()) {
                try {
                    sleep(20);
                }
                catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                artists.add(new Artist(res.getInt(INDEX_ARTISTS_ID), res.getString(INDEX_ARTISTS_NAME)));
            }
            return artists;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }

    }

    public List<Album> queryAlbumsByArtistId(int artistId) {
        try {
            queryAlbumsByArtistId.setInt(1, artistId);
            ResultSet res = queryAlbumsByArtistId.executeQuery();
            List<Album> albums = new ArrayList<>();
            while (res.next()) {
                try {
                    sleep(200);
                }
                catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                albums.add(new Album(res.getInt(1), res.getString(2), artistId));
            }
            return albums;
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public boolean updateArtist(int artistId, String newName) {
        try {
            updateArtistStmt.setString(1, newName);
            updateArtistStmt.setInt(2, artistId);
            int affectedRows = updateArtistStmt.executeUpdate();
            if (affectedRows != 1) {
                System.out.println("Not good");
                return false;
            }
            return true;
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public List<String> queryAlbumsForArtist(String artist, int order) {
        StringBuilder sb = new StringBuilder(QUERY_ALBUMS_FROM_ARTIST_START + String.format("'%s'", artist));
        if (order != ORDER_BY_NONE) {
            sb.append(QUERY_ALBUMS_FROM_ARTIST_ORDER + (order == ORDER_BY_DESC ? " desc" : " asc"));
        }
        try (Statement statement = conn.createStatement();
             ResultSet res = statement.executeQuery(sb.toString())) {
            List<String> albums = new ArrayList<>();
            while (res.next()) {
                albums.add(res.getString(1));
            }
            return albums;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void querySongsMetadata() {
        String sql = "SELECT * FROM " + TABLE_SONGS;
        try (Statement statement = conn.createStatement(); ResultSet res = statement.executeQuery(sql)) {
            ResultSetMetaData meta = res.getMetaData();
            int numColumns = meta.getColumnCount();
            for (int i = 1; i <= numColumns; i++) {
                System.out.printf("Column %d is %s\n", i, meta.getColumnName(i));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public boolean createView() {
        try (Statement stmt = conn.createStatement()) {
            System.out.println(VIEW_ARTIST_LIST_START);
            stmt.execute(VIEW_ARTIST_LIST_START);
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public int getCount(String table) {
        String sql = "SELECT count(*) AS count, MIN(_id) AS min_id FROM " + table;
        try (Statement statement = conn.createStatement(); ResultSet res = statement.executeQuery(sql)) {
            return res.getInt(1);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    private int insertArtist(String name) throws SQLException {
        // check to see if artist exists, if exists method will return the id of that artist
        queryArtist.setString(1, name);
        ResultSet res = queryArtist.executeQuery();
        if (res.next()) {
            // record exists
            return res.getInt(1);
        }


        insertArtist.setString(1, name);
        int affectedRows = insertArtist.executeUpdate();
        if (affectedRows != 1) {
            throw new SQLException("Can't insert artist.");
        }
        res = insertArtist.getGeneratedKeys();
        res.next();
        return res.getInt(1);

    }

    private int insertAlbum(String name, int artist) throws SQLException {
        // check to see if album exists, if exists method will return the id of that album
        queryAlbum.setString(1, name);
        ResultSet res = queryAlbum.executeQuery();
        if (res.next()) {
            // record exists
            return res.getInt(1);
        }

        insertAlbum.setString(1, name);
        insertAlbum.setInt(2, artist);
        int affectedRows = insertAlbum.executeUpdate();
        if (affectedRows != 1) {
            throw new SQLException("Can't insert album.");
        }
        res = insertAlbum.getGeneratedKeys();
        if (res.next())
            return res.getInt(1);
        return -1;
    }

    public void insertSong(String title, String album, String artist, int track) {
        try {
            conn.setAutoCommit(false);

            int artistId = insertArtist(artist);
            int albumId = insertAlbum(album, artistId);
            insertSong.setInt(1, track);
            insertSong.setString(2, title);
            insertSong.setInt(3, albumId);
            int affectedRows = insertSong.executeUpdate();
            if (affectedRows == 1) {
                conn.commit();
                return;
            }
            throw new SQLException("Can't insert song.");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            try {
                System.out.println("Performing rollback");
                conn.rollback();
            }
            catch (SQLException ex2) {
                System.out.println("Oh boy! Things are really bad!");
                ex2.printStackTrace();
            }
        }
        finally {
            try {
                System.out.println("Setting autocommit to true");
                conn.setAutoCommit(true);
            }
            catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void query() {
        String sql = "DELETE FROM songs WHERE title = \"New Song\"";
        String sql2 = "DELETE FROM albums WHERE name = \"NoAlbum\"";
        String sql3 = "DELETE FROM artists WHERE name = \"NoArtist\"";
        try (Statement stmt = conn.createStatement()) {
           stmt.execute(sql);
           stmt.execute(sql2);
           stmt.execute(sql3);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
//        try {
//            PreparedStatement prep = conn.prepareStatement("SELECT name FROM artists");
////            prep.setString(1, "name");
//            ResultSet res = prep.executeQuery();
//            res.next();
//            System.out.println(res.getString(1));
//        }
//        catch (SQLException ex) {
//            ex.printStackTrace();
//        }
    }
}
