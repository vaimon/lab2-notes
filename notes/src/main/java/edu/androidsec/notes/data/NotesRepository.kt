package edu.androidsec.notes.data

import edu.androidsec.notes.data.db.NoteDao
import edu.androidsec.notes.data.db.toDomain
import edu.androidsec.notes.data.db.toEntity
import edu.androidsec.notes.domain.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Порт доступа к заметкам. ViewModel-и зависят только от него. */
interface NotesRepository {
    fun observeAll(): Flow<List<Note>>
    suspend fun get(id: Long): Note?
    /** Сохраняет заметку и возвращает её id (для новой заметки — только что выданный). */
    suspend fun save(note: Note): Long
    suspend fun delete(id: Long)
}

class RoomNotesRepository(private val dao: NoteDao) : NotesRepository {

    override fun observeAll(): Flow<List<Note>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun get(id: Long): Note? = dao.getById(id)?.toDomain()

    override suspend fun save(note: Note): Long {
        val stamped = note.copy(updatedAt = System.currentTimeMillis())
        val rowId = dao.upsert(stamped.toEntity())
        return if (note.isNew) rowId else note.id
    }

    override suspend fun delete(id: Long) = dao.deleteById(id)
}
