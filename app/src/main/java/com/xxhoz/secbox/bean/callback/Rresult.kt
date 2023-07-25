package com.xxhoz.secbox.bean.callback

/**
 *
 * @author DengNanYu
 * @version 1.0_2023/7/17
 * @date 2023/7/17 10:57
 */
object Rresult {

    fun oK(): Rdata {
        return ok("ok", null)
    }

    fun oK(obj: Any): Rdata {
        return ok("ok", obj)
    }

    fun fail(obj: Any): Rdata {
        return fail("fail", obj)
    }


    fun ok(mes: String?, obj: Any?): Rdata {
        return Rdata(Rstate.SUCCESS, mes!!, obj)
    }

    fun fail(mes: String?, obj: Any): Rdata {
        return Rdata(Rstate.SUCCESS, mes!!, obj)
    }

    fun error(mes: String?, obj: Any): Rdata {
        return Rdata(Rstate.SUCCESS, mes!!, obj)
    }
}
