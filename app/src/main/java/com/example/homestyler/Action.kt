package com.example.homestyler

sealed class Action {
    object Click : Action()
    object LongPress : Action()
}
