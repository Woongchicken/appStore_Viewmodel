package com.example.appstore.Room

import androidx.room.TypeConverter


class DataListConverters {
    /*    Room Database에서는 기본적으로 몇 가지 기본 타입들을 자동으로 처리할 수 있지만, 커스텀한 타입이나 복잡한 데이터 구조는 직접 타입 컨버터를 정의해야 합니다.
    타입 컨버터를 통해 해당 필드를 데이터베이스에서 사용할 수 있는 형식으로 변환할 수 있습니다.*/

    @TypeConverter
    fun listToJson(list: List<String>): String{
        return list.joinToString(",")
    }

    @TypeConverter
    fun jsonToList(string: String): List<String>{
        return string.split(",")
    }

}