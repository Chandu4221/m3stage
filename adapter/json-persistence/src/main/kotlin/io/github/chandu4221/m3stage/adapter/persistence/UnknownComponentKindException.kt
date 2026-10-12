package io.github.chandu4221.m3stage.adapter.persistence

class UnknownComponentKindException(val kindId: String) :
    IllegalArgumentException("Unknown or unsupported component kind '$kindId'")
