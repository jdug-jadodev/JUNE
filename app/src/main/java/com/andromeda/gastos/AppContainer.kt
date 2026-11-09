package com.andromeda.gastos

import android.content.Context
import com.andromeda.gastos.application.usecase.ActualizarGastoUseCase
import com.andromeda.gastos.application.usecase.AutenticacionUseCase
import com.andromeda.gastos.application.usecase.ClasificarTerminoUseCase
import com.andromeda.gastos.application.usecase.ConsultarAnalisisUseCase
import com.andromeda.gastos.application.usecase.ConsultarGastosUseCase
import com.andromeda.gastos.application.usecase.EliminarGastoUseCase
import com.andromeda.gastos.application.usecase.ExportarGastosUseCase
import com.andromeda.gastos.application.usecase.GestionarCategoriasUseCase
import com.andromeda.gastos.application.usecase.GestionarHogarUseCase
import com.andromeda.gastos.application.usecase.GestionarIngresosUseCase
import com.andromeda.gastos.application.usecase.GestionarMiembrosUseCase
import com.andromeda.gastos.application.usecase.RegistrarGastoPorVozUseCase
import com.andromeda.gastos.application.usecase.RegistrarGastoUseCase
import com.andromeda.gastos.application.usecase.SeguridadUseCase
import com.andromeda.gastos.domain.port.portin.ActualizarGastoPort
import com.andromeda.gastos.domain.port.portin.AutenticacionPort
import com.andromeda.gastos.domain.port.portin.ClasificarTerminoPort
import com.andromeda.gastos.domain.port.portin.ConsultarAnalisisPort
import com.andromeda.gastos.domain.port.portin.ConsultarGastosPort
import com.andromeda.gastos.domain.port.portin.EliminarGastoPort
import com.andromeda.gastos.domain.port.portin.ExportarGastosPort
import com.andromeda.gastos.domain.port.portin.GestionarCategoriasPort
import com.andromeda.gastos.domain.port.portin.GestionarHogarPort
import com.andromeda.gastos.domain.port.portin.GestionarIngresosPort
import com.andromeda.gastos.domain.port.portin.GestionarMiembrosPort
import com.andromeda.gastos.domain.port.portin.RegistrarGastoPorVozPort
import com.andromeda.gastos.domain.port.portin.RegistrarGastoPort
import com.andromeda.gastos.domain.port.portin.SeguridadPort
import com.andromeda.gastos.infrastructure.export.CsvShareAdapter
import com.andromeda.gastos.infrastructure.local.CacheDatos
import com.andromeda.gastos.infrastructure.local.SesionDataStore
import com.andromeda.gastos.infrastructure.remote.ApiClient
import com.andromeda.gastos.infrastructure.remote.AnalisisApiAdapter
import com.andromeda.gastos.infrastructure.remote.AuthApiAdapter
import com.andromeda.gastos.infrastructure.remote.CategoriaApiAdapter
import com.andromeda.gastos.infrastructure.remote.ExportacionApiAdapter
import com.andromeda.gastos.infrastructure.remote.GastoApiAdapter
import com.andromeda.gastos.infrastructure.remote.HogarApiAdapter
import com.andromeda.gastos.infrastructure.remote.IngresoApiAdapter
import com.andromeda.gastos.infrastructure.remote.MiembroApiAdapter
import com.andromeda.gastos.infrastructure.remote.SeguridadApiAdapter
import com.andromeda.gastos.infrastructure.voz.AndroidVozAdapter

class AppContainer(context: Context) {

    private val sesionLocal = SesionDataStore(context)
    private val apiClient = ApiClient(BuildConfig.API_BASE_URL, sesionLocal)
    private val cache = CacheDatos()

    private val authRemoto = AuthApiAdapter(apiClient)
    private val hogarRepo = HogarApiAdapter(apiClient, cache)
    private val miembroRepo = MiembroApiAdapter(apiClient, cache)
    private val gastoRepo = GastoApiAdapter(apiClient)
    private val analisisRepo = AnalisisApiAdapter(apiClient)
    private val categoriaRepo = CategoriaApiAdapter(apiClient)
    private val ingresoRepo = IngresoApiAdapter(apiClient)
    private val exportRemoto = ExportacionApiAdapter(apiClient)
    private val exportLocal = CsvShareAdapter(context)
    private val seguridadRemoto = SeguridadApiAdapter(apiClient)
    private val voz = AndroidVozAdapter(context)

    val autenticacion: AutenticacionPort = AutenticacionUseCase(authRemoto, sesionLocal, cache)
    val gestionarHogar: GestionarHogarPort = GestionarHogarUseCase(hogarRepo)
    val gestionarMiembros: GestionarMiembrosPort = GestionarMiembrosUseCase(miembroRepo)
    val registrarGasto: RegistrarGastoPort = RegistrarGastoUseCase(gastoRepo)
    val actualizarGasto: ActualizarGastoPort = ActualizarGastoUseCase(gastoRepo)
    val eliminarGasto: EliminarGastoPort = EliminarGastoUseCase(gastoRepo)
    val consultarGastos: ConsultarGastosPort = ConsultarGastosUseCase(gastoRepo)
    val consultarAnalisis: ConsultarAnalisisPort = ConsultarAnalisisUseCase(analisisRepo)
    val clasificarTermino: ClasificarTerminoPort = ClasificarTerminoUseCase(analisisRepo)
    val gestionarCategorias: GestionarCategoriasPort = GestionarCategoriasUseCase(categoriaRepo)
    val gestionarIngresos: GestionarIngresosPort = GestionarIngresosUseCase(ingresoRepo)
    val exportarGastos: ExportarGastosPort = ExportarGastosUseCase(exportRemoto, exportLocal)
    val seguridad: SeguridadPort = SeguridadUseCase(seguridadRemoto)
    val registrarGastoPorVoz: RegistrarGastoPorVozPort =
        RegistrarGastoPorVozUseCase(voz, registrarGasto)

    fun detenerVoz() {
        voz.detener()
    }
}
