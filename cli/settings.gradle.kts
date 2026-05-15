rootProject.name = "modweave-cli"
includeBuild("..") {
  dependencySubstitution {
    substitute(module("git.walhay:app")).using(project(":app"))
    substitute(module("git.walhay:ui")).using(project(":ui"))
    substitute(module("git.walhay:business-logic")).using(project(":business-logic"))
    substitute(module("git.walhay:data-access")).using(project(":data-access"))
  }
}
