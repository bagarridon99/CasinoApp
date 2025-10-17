package com.example.casinoapp.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.casinoapp.data.entity.UserEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class UserDao_Impl implements UserDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<UserEntity> __insertionAdapterOfUserEntity;

  private final EntityDeletionOrUpdateAdapter<UserEntity> __updateAdapterOfUserEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdatePassword;

  private final SharedSQLiteStatement __preparedStmtOfSetRecovery;

  public UserDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUserEntity = new EntityInsertionAdapter<UserEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `users` (`id`,`email`,`passwordHash`,`passwordSalt`,`recoveryCode`,`recoveryCodeExpiresAt`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getEmail());
        statement.bindString(3, entity.getPasswordHash());
        statement.bindString(4, entity.getPasswordSalt());
        if (entity.getRecoveryCode() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getRecoveryCode());
        }
        if (entity.getRecoveryCodeExpiresAt() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getRecoveryCodeExpiresAt());
        }
      }
    };
    this.__updateAdapterOfUserEntity = new EntityDeletionOrUpdateAdapter<UserEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `users` SET `id` = ?,`email` = ?,`passwordHash` = ?,`passwordSalt` = ?,`recoveryCode` = ?,`recoveryCodeExpiresAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getEmail());
        statement.bindString(3, entity.getPasswordHash());
        statement.bindString(4, entity.getPasswordSalt());
        if (entity.getRecoveryCode() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getRecoveryCode());
        }
        if (entity.getRecoveryCodeExpiresAt() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getRecoveryCodeExpiresAt());
        }
        statement.bindLong(7, entity.getId());
      }
    };
    this.__preparedStmtOfUpdatePassword = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE users SET passwordHash=?, passwordSalt=?,\n"
                + "        recoveryCode=NULL, recoveryCodeExpiresAt=NULL WHERE id=?\n"
                + "    ";
        return _query;
      }
    };
    this.__preparedStmtOfSetRecovery = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE users SET recoveryCode=?, recoveryCodeExpiresAt=? WHERE id=?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final UserEntity user, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfUserEntity.insertAndReturnId(user);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final UserEntity user, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfUserEntity.handle(user);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePassword(final long userId, final String hash, final String salt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdatePassword.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, hash);
        _argIndex = 2;
        _stmt.bindString(_argIndex, salt);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, userId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdatePassword.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object setRecovery(final long userId, final String code, final long expiresAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetRecovery.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, code);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, expiresAt);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, userId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSetRecovery.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getByEmail(final String email, final Continuation<? super UserEntity> $completion) {
    final String _sql = "SELECT * FROM users WHERE email = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, email);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<UserEntity>() {
      @Override
      @Nullable
      public UserEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfPasswordHash = CursorUtil.getColumnIndexOrThrow(_cursor, "passwordHash");
          final int _cursorIndexOfPasswordSalt = CursorUtil.getColumnIndexOrThrow(_cursor, "passwordSalt");
          final int _cursorIndexOfRecoveryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "recoveryCode");
          final int _cursorIndexOfRecoveryCodeExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "recoveryCodeExpiresAt");
          final UserEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpPasswordHash;
            _tmpPasswordHash = _cursor.getString(_cursorIndexOfPasswordHash);
            final String _tmpPasswordSalt;
            _tmpPasswordSalt = _cursor.getString(_cursorIndexOfPasswordSalt);
            final String _tmpRecoveryCode;
            if (_cursor.isNull(_cursorIndexOfRecoveryCode)) {
              _tmpRecoveryCode = null;
            } else {
              _tmpRecoveryCode = _cursor.getString(_cursorIndexOfRecoveryCode);
            }
            final Long _tmpRecoveryCodeExpiresAt;
            if (_cursor.isNull(_cursorIndexOfRecoveryCodeExpiresAt)) {
              _tmpRecoveryCodeExpiresAt = null;
            } else {
              _tmpRecoveryCodeExpiresAt = _cursor.getLong(_cursorIndexOfRecoveryCodeExpiresAt);
            }
            _result = new UserEntity(_tmpId,_tmpEmail,_tmpPasswordHash,_tmpPasswordSalt,_tmpRecoveryCode,_tmpRecoveryCodeExpiresAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
