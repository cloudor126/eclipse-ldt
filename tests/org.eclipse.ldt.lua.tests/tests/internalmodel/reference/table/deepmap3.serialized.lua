do local _={
		unknownglobalvars={

		}
		--[[table: 0x97e1188]],
		content={
			localvars={
				[1]={
					item={
						type={
							def={
								shortdescription="",
								name="table",
								fields={
									othertable={
										type={
											def={
												shortdescription="",
												name="table",
												fields={
													fieldname={
														type={
															typename="number",
															tag="primitivetyperef"
														}
														--[[table: 0x98f9730]],
														description="",
														parent=nil --[[ref]],
														shortdescription="",
														name="fieldname",
														sourcerange={
															min=69,
															max=77
														}
														--[[table: 0x93b40c8]],
														occurrences={

														}
														--[[table: 0x93b40a0]],
														tag="item"
													}
													--[[table: 0x93b4078]]
												}
												--[[table: 0x98a91d0]],
												sourcerange={
													min=0,
													max=0
												}
												--[[table: 0x98a91f8]],
												description="",
												tag="recordtypedef"
											}
											--[[table: 0x98a91a8]],
											tag="inlinetyperef"
										}
										--[[table: 0x97a5098]],
										description="",
										parent=nil --[[ref]],
										shortdescription="",
										name="othertable",
										sourcerange={
											min=32,
											max=41
										}
										--[[table: 0x98d3448]],
										occurrences={

										}
										--[[table: 0x97a50e8]],
										tag="item"
									}
									--[[table: 0x97a50c0]]
								}
								--[[table: 0x9408538]],
								sourcerange={
									min=0,
									max=0
								}
								--[[table: 0x9408560]],
								description="",
								tag="recordtypedef"
							}
							--[[table: 0x9754500]],
							tag="inlinetyperef"
						}
						--[[table: 0x9408588]],
						description="",
						shortdescription="",
						name="tablename",
						sourcerange={
							min=7,
							max=15
						}
						--[[table: 0x93c3300]],
						occurrences={
							[1]={
								sourcerange={
									min=7,
									max=15
								}
								--[[table: 0x9615688]],
								definition=nil --[[ref]],
								tag="MIdentifier"
							}
							--[[table: 0x9615660]],
							[2]={
								sourcerange={
									min=22,
									max=30
								}
								--[[table: 0x9888f90]],
								definition=nil --[[ref]],
								tag="MIdentifier"
							}
							--[[table: 0x9888f68]],
							[3]={
								sourcerange={
									min=48,
									max=56
								}
								--[[table: 0x981db10]],
								definition=nil --[[ref]],
								tag="MIdentifier"
							}
							--[[table: 0x981dae8]]
						}
						--[[table: 0x93c32d8]],
						tag="item"
					}
					--[[table: 0x95c59e0]],
					scope={
						min=0,
						max=0
					}
					--[[table: 0x967fe48]]
				}
				--[[table: 0x9886d68]]
			}
			--[[table: 0x9579020]],
			sourcerange={
				min=1,
				max=10000
			}
			--[[table: 0x9579048]],
			content={
				[1]=nil --[[ref]],
				[2]={
					sourcerange={
						min=22,
						max=41
					}
					--[[table: 0x9888fb8]],
					right="othertable",
					left=nil --[[ref]],
					tag="MIndex"
				}
				--[[table: 0x96156b0]],
				[3]={
					sourcerange={
						min=48,
						max=77
					}
					--[[table: 0x957c6e0]],
					right="fieldname",
					left={
						sourcerange={
							min=48,
							max=67
						}
						--[[table: 0x95d6440]],
						right="othertable",
						left=nil --[[ref]],
						tag="MIndex"
					}
					--[[table: 0x95d6418]],
					tag="MIndex"
				}
				--[[table: 0x95d6468]]
			}
			--[[table: 0x9578ff8]],
			tag="MBlock"
		}
		--[[table: 0x97e11b0]],
		tag="MInternalContent"
	}
	--[[table: 0x97e1160]];
	_.content.localvars[1].item.type.def.fields.othertable.type.def.fields.fieldname.parent=_.content.localvars[1].item.type.def.fields.othertable.type.def;
	_.content.localvars[1].item.type.def.fields.othertable.parent=_.content.localvars[1].item.type.def;
	_.content.localvars[1].item.occurrences[1].definition=_.content.localvars[1].item;
	_.content.localvars[1].item.occurrences[2].definition=_.content.localvars[1].item;
	_.content.localvars[1].item.occurrences[3].definition=_.content.localvars[1].item;
	_.content.content[1]=_.content.localvars[1].item.occurrences[1];
	_.content.content[2].left=_.content.localvars[1].item.occurrences[2];
	_.content.content[3].left.left=_.content.localvars[1].item.occurrences[3];
	return _;
end