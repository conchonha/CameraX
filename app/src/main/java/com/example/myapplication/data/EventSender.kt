package com.example.myapplication.data

sealed class EventSender{
    class ShowToast(val message : Int) : EventSender()
}