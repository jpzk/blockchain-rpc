/**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.madewithtea.blockchainrpc.examples.ethereum

import cats.effect.{ExitCode, IO, IOApp}
import com.madewithtea.blockchainrpc.ethereum.Syntax._
import com.madewithtea.blockchainrpc.ethereum.HexTools
import com.madewithtea.blockchainrpc.ethereum.Protocol.TransactionResponse
import com.madewithtea.blockchainrpc.{BatchResponse, Config, Ethereum, RPCClient}

import scala.concurrent.ExecutionContext.global

object CatchupFromZero extends IOApp {

  def getReceipts(rpc: Ethereum, txs: Seq[String]) =
    for {
      receipts <- rpc.getReceiptsByHash(txs)
      _ <- IO(println(s"${receipts.seq.size} receipts"))
    } yield ()

  def loop(rpc: Ethereum, current: Long = 9000000L, until: Long = 9120000): IO[Unit] =
    for {
      block <- rpc.getBlockByHeight(current)
      _ <- IO { println(s"block ${HexTools.parseQuantity(block.number)} - ${block.hash}: ${block.transactions.size} transactions") }
      transactions <- if(block.transactions.nonEmpty) rpc.getTransactions(block.transactions) else IO.pure(BatchResponse[TransactionResponse](List()))
      _ <- IO(println(s"transactions: ${transactions.seq.size}"))
      _ <- if(block.transactions.nonEmpty) getReceipts(rpc, block.transactions) else IO.unit
      l <- if (current + 1 < until) loop(rpc, current + 1, until) else IO.unit
    } yield l

  def run(args: List[String]): IO[ExitCode] = {
    implicit val ec = global
    implicit val config = Config.fromEnv
    RPCClient
      .ethereum(
        config.hosts,
        config.port,
        config.username,
        config.password,
        onErrorRetry = { (_, e: Throwable) =>
          IO(println(e))
        }
      )
      .use { ethereum =>
        for {
          _ <- loop(ethereum)
        } yield ExitCode(0)
      }
  }
}
