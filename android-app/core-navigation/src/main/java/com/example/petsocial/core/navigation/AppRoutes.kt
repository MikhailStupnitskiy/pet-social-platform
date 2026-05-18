package com.example.petsocial.core.navigation

object AppRoutes {
    const val Loading = "loading"
    const val SessionError = "session_error"
    const val Auth = "auth"

    const val Profile = "profile"
    const val Matching = "matching"
    const val Chats = "chats"
    const val Care = "care"
    const val Feed = "feed"
    const val Handlers = "handlers"
    const val Notifications = "notifications"

    const val PublicUserArg = "userId"
    const val PublicPetArg = "petId"
    const val PublicUser = "public_user/{$PublicUserArg}"
    const val PublicPet = "public_pet/{$PublicPetArg}"

    fun publicUser(userId: String): String = "public_user/$userId"

    fun publicPet(petId: String): String = "public_pet/$petId"
}
