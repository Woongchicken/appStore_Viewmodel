package com.example.appstore.Room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [HistoryEntity::class], version = 2, exportSchema = false)
@TypeConverters(DataListConverters::class)
abstract class RoomDB : RoomDatabase() {
    abstract fun mainDao(): MainDao

    companion object {
        @Volatile   // 해당 변수가 여러 스레드 사이에서의 가시성을 보장 - 변수의 값을 변경하면, 이 변경 내용은 모든 스레드에게 즉시 알려짐. ->  다중 스레드 환경에서 변수의 가시성과 순서를 제어할 수 있고, 변수의 값을 안정적이고 일관되게 사용할 수 있음.
        private var database: RoomDB? = null

        // Roomdb가 필요할때마다 호출.  공유 자원에 대한 동시 접근을 제어하여, 데이터의 일관성과 동시성 보장
        fun getInstance(context: Context): RoomDB {
            return database ?: synchronized(this) {      // null이 아닌 경우에는 기존 인스턴스를 반환
                database ?: buildDatabase(context)            // null인 경우에만 buildDatabase() 함수가 호출되어 새로운 인스턴스를 생성  -> 데이터베이스 인스턴스의 생성은 최초의 한 번
            }
        }

        private fun buildDatabase(context: Context): RoomDB {
            return Room.databaseBuilder(
                context.applicationContext,
                RoomDB::class.java,
                "Woong"
            )
                .allowMainThreadQueries()       // Room 데이터베이스에서 메인 스레드(main thread)에서 쿼리를 수행할 수 있도록 허용,  테스트 용도로만 권장, 실제 앱에서는 사용하지 않는 것이 좋음.
                .fallbackToDestructiveMigration()  // 스키마 버전이 변경되었을 때, 이전 버전의 데이터를 보존하지 않고 데이터베이스를 파괴하고 새로운 스키마로 다시 생성하는 것을 허용. 앱을 업데이트하여 데이터베이스 스키마를 변경하는 경우, 이전 버전의 데이터를 보존할 필요가 없다면 fallbackToDestructiveMigration()를 사용하여 간단하게 스키마를 업데이트 가능
                .build()
        }
    }


}