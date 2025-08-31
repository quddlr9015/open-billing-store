package io.byungik.open_billing_store.repository

import io.byungik.open_billing_store.entity.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {
    fun findByParentIsNullAndIsActiveTrue(): List<Category>
    fun findByParentIdAndIsActiveTrue(parentId: Long): List<Category>
    fun findByIsActiveTrue(): List<Category>
}