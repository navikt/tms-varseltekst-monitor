object Apache: default.DependencyGroup {
    override val groupId = "org.apache.poi"
    override val version = "5.4.1"

    val poi = dependency("poi")
    val ooxml = dependency("poi-ooxml")
}
